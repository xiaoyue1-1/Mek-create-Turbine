package com.mekcreateturbine.common.content.turbine;

import com.mekcreateturbine.common.content.mode.OutputMode;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineValve;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineVent;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.util.CableUtils;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Runtime data for our mechanical turbine multiblock (dual output mode).
 *
 * <p>Mirrors Mek 1.21 {@code TurbineMultiblockData} but the {@code MECHANICAL} branch never
 * touches the energy container: steam flow is throttled by the Create load utilisation
 * ({@link #setMechanicalLoad}) and no energy is inserted/stored, so the turbine can never output
 * electricity while in mechanical mode.
 */
public class MechanicalTurbineData extends MultiblockData {

    public static final float ROTATION_THRESHOLD = 0.001F;
    public static final Object2FloatMap<UUID> clientRotationMap = new Object2FloatOpenHashMap<>();

    @SyntheticComputerMethod(getter = "getOutputMode")
    public OutputMode outputMode = OutputMode.ELECTRICAL;

    @ContainerSync
    public GasMode dumpMode = GasMode.IDLE;

    @ContainerSync
    public IChemicalTank chemicalTank;
    @ContainerSync
    public IExtendedFluidTank ventTank;
    @ContainerSync
    public IEnergyContainer energyContainer;
    private long energyCapacity = 0;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getBlades")
    public int blades;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getVents")
    public int vents;
    private List<VentData> ventData = Collections.emptyList();
    @ContainerSync
    @SyntheticComputerMethod(getter = "getCoils")
    public int coils;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getCondensers")
    public int condensers;
    @ContainerSync
    public int lowerVolume;

    public BlockPos complex;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getLastSteamInputRate")
    public long lastSteamInput;
    public long newSteamInput;

    @ContainerSync
    @SyntheticComputerMethod(getter = "getFlowRate")
    public long clientFlow;

    /** 0..1 load utilisation reported back from the Create output node (server side). */
    public float mechanicalLoad;

    /** Structure flags, recomputed on every (re)validation. */
    public boolean centerCoil;
    public boolean hasCoils;
    public boolean mechanicalShaft;

    public float clientRotation;
    public float prevSteamScale;

    /** Client-side values sent in the block-entity update tag for the GUI. */
    public long clientStressMax;
    public long clientStressCur;

    public MechanicalTurbineData(TileEntityMechanicalTurbineCasing tile) {
        super(tile);
        chemicalTanks.add(chemicalTank = new MechanicalTurbineChemicalTank(this, createSaveAndComparator()));
        fluidTanks.add(ventTank = VariableCapacityFluidTank.output(this,
              () -> isFormed() ? condensers * MekanismGeneratorsConfig.generators.condenserRate.get() : FluidType.BUCKET_VOLUME,
              fluid -> fluid.is(FluidTags.WATER), this));
        energyContainer = VariableCapacityEnergyContainer.create(this::getEnergyCapacity, automationType -> isFormed(),
              automationType -> automationType == AutomationType.INTERNAL && isFormed(), this);
        energyContainers.add(energyContainer);
    }

    @Override
    protected void updateEjectors(Level world) {
        fluidTargets.clear();
        energyTargets.clear();
        for (ValveData valve : valves) {
            TileEntityMechanicalTurbineValve tile = WorldUtils.getTileEntity(TileEntityMechanicalTurbineValve.class, world, valve.location);
            if (tile != null) {
                tile.addEnergyTargetCapability(energyOutputTargets(), valve.side);
            }
        }
        for (VentData data : ventData) {
            TileEntityMechanicalTurbineVent vent = WorldUtils.getTileEntity(TileEntityMechanicalTurbineVent.class, world, data.location);
            if (vent != null) {
                vent.addFluidTargetCapability(fluidOutputTargets(), data.side);
            }
        }
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);

        lastSteamInput = newSteamInput;
        newSteamInput = 0;
        long stored = chemicalTank.getStored();
        double flowRate = 0;

        boolean mechanical = outputMode == OutputMode.MECHANICAL;
        long energyNeeded = energyContainer.getNeeded();
        double energyMultiplier = ((double) MekanismConfig.general.maxEnergyPerSteam.get() / MechanicalTurbineValidator.MAX_BLADES)
                                  * (Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
        boolean able;
        if (mechanical) {
            // Stress output: driven purely by the load utilisation reported by the Create node.
            able = stored > 0 && mechanicalLoad > 0;
        } else {
            able = stored > 0 && energyMultiplier >= Mth.EPSILON && energyNeeded > 0;
        }
        if (able) {
            double rate = lowerVolume * (getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserChemicalFlow.get());
            rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentChemicalFlow.get());
            double proportion = stored / (double) getSteamCapacity();
            double origRate = rate;
            double cap = Math.min(stored, rate);
            if (mechanical) {
                cap *= proportion * Math.min(1, mechanicalLoad);
            } else {
                cap = Math.min(cap, energyNeeded / energyMultiplier) * proportion;
            }
            clientFlow = MathUtils.clampToLong(cap);
            if (clientFlow > 0) {
                flowRate = cap / origRate;
                if (!mechanical) {
                    energyContainer.insert(MathUtils.clampToLong(energyMultiplier * cap), Action.EXECUTE, AutomationType.INTERNAL);
                }
                chemicalTank.shrinkStack(clientFlow, Action.EXECUTE);
                ventTank.insert(new FluidStack(Fluids.WATER, Math.min(MathUtils.clampToInt(cap),
                      condensers * MekanismGeneratorsConfig.generators.condenserRate.get())), Action.EXECUTE, AutomationType.INTERNAL);
            }
        } else {
            clientFlow = 0;
        }
        // Steam dump modes (IDLE / DUMPING_EXCESS / DUMPING), like the stock turbine.
        if (dumpMode != GasMode.IDLE && !chemicalTank.isEmpty()) {
            long amount = chemicalTank.getStored();
            if (dumpMode == GasMode.DUMPING) {
                chemicalTank.shrinkStack(getDumpingAmount(amount), Action.EXECUTE);
            } else {
                long target = MathUtils.clampToLong(chemicalTank.getCapacity() * MekanismConfig.general.dumpExcessKeepRatio.get());
                if (target < amount) {
                    chemicalTank.shrinkStack(Math.min(amount - target, getDumpingAmount(amount)), Action.EXECUTE);
                }
            }
        }
        // Water venting
        if (!fluidOutputTargets().isEmpty() && !ventTank.isEmpty()) {
            ventTank.extract(FluidUtils.emit(fluidOutputTargets(), ventTank.getFluid()), Action.EXECUTE, AutomationType.INTERNAL);
        }
        // Electricity output only in ELECTRICAL mode; mechanical keeps the buffer untouched but never emits.
        if (!mechanical) {
            CableUtils.emit(energyOutputTargets(), energyContainer);
        }

        float newRotation = (float) flowRate;
        if (Math.abs(newRotation - clientRotation) > ROTATION_THRESHOLD) {
            clientRotation = newRotation;
            needsPacket = true;
        }
        float scale = MekanismUtils.getScale(prevSteamScale, chemicalTank);
        if (MekanismUtils.scaleChanged(scale, prevSteamScale)) {
            needsPacket = true;
            prevSteamScale = scale;
        }
        return needsPacket;
    }

    public void updateVentData(List<VentData> ventData) {
        this.ventData = ventData;
        this.vents = this.ventData.size();
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE, scale -> prevSteamScale = scale);
        NBTUtils.setIntIfPresent(tag, SerializationConstants.LOWER_VOLUME, value -> lowerVolume = value);
        NBTUtils.setChemicalStackIfPresent(provider, tag, SerializationConstants.CHEMICAL, value -> chemicalTank.setStack(value));
        NBTUtils.setFluidStackIfPresent(provider, tag, SerializationConstants.FLUID, ventTank::setStack);
        NBTUtils.setBlockPosIfPresent(tag, SerializationConstants.COMPLEX, value -> complex = value);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.ROTATION, value -> clientRotation = value);
        if (tag.contains("OutputMode")) {
            String stored = tag.getString("OutputMode");
            for (OutputMode mode : OutputMode.values()) {
                if (mode.name().equals(stored)) {
                    outputMode = mode;
                    break;
                }
            }
        }
        if (tag.contains("StressMax")) {
            clientStressMax = tag.getLong("StressMax");
        }
        if (tag.contains("StressCur")) {
            clientStressCur = tag.getLong("StressCur");
        }
        if (tag.contains("DumpMode")) {
            String stored = tag.getString("DumpMode");
            for (GasMode mode : GasMode.values()) {
                if (mode.name().equals(stored)) {
                    dumpMode = mode;
                    break;
                }
            }
        }
        // Keep reused Mek turbine rotors animating: their renderer only reads this static map.
        clientRotationMap.put(inventoryID, clientRotation);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        tag.putFloat(SerializationConstants.SCALE, prevSteamScale);
        tag.putInt(SerializationConstants.LOWER_VOLUME, lowerVolume);
        tag.put(SerializationConstants.CHEMICAL, chemicalTank.getStack().saveOptional(provider));
        tag.put(SerializationConstants.FLUID, ventTank.getFluid().saveOptional(provider));
        tag.put(SerializationConstants.COMPLEX, NbtUtils.writeBlockPos(complex));
        tag.putFloat(SerializationConstants.ROTATION, clientRotation);
        tag.putString("OutputMode", outputMode.name());
        double ratio = com.mekcreateturbine.common.config.MCTConfig.MECHANICAL_STRESS_PER_FLOW_RATE.get();
        tag.putLong("StressMax", (long) (getMaxFlowRate() * ratio));
        tag.putLong("StressCur", getMechanicalStressCapacity());
        tag.putString("DumpMode", dumpMode.name());
    }

    @ComputerMethod
    public int getDispersers() {
        return (length() - 2) * (width() - 2) - 1;
    }

    private long getDumpingAmount(long stored) {
        return Math.min(stored, Math.max(stored / 50, lastSteamInput * 2));
    }

    public long getSteamCapacity() {
        return lowerVolume * MekanismGeneratorsConfig.generators.turbineChemicalPerTank.get();
    }

    public long getEnergyCapacity() {
        return energyCapacity;
    }

    @Override
    public void setVolume(int volume) {
        if (getVolume() != volume) {
            super.setVolume(volume);
            energyCapacity = volume * MekanismGeneratorsConfig.generators.turbineEnergyCapacityPerVolume.get();
        }
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(chemicalTank.getStored(), chemicalTank.getCapacity());
    }

    @ComputerMethod
    public OutputMode getOutputMode() {
        return outputMode;
    }

    /** Whether the given output mode is possible with the current structure layout. */
    public boolean isModeAllowed(OutputMode mode) {
        if (mode == OutputMode.MECHANICAL) {
            return mechanicalShaft && !centerCoil;
        }
        return hasCoils;
    }

    /** Picks the best mode available right now (called by the validator after recomputing flags). */
    public void resolveDefaultMode() {
        if (!isModeAllowed(outputMode)) {
            if (isModeAllowed(OutputMode.ELECTRICAL)) {
                setOutputMode(OutputMode.ELECTRICAL);
            } else if (isModeAllowed(OutputMode.MECHANICAL)) {
                setOutputMode(OutputMode.MECHANICAL);
            }
        }
    }

    @ComputerMethod(nameOverride = "setOutputMode")
    public void setOutputMode(OutputMode mode) {
        if (mode == outputMode || !isModeAllowed(mode)) {
            return;
        }
        outputMode = mode;
        // In MECHANICAL mode we neither insert nor output electricity; any leftover buffer is kept
        // and becomes usable again when switching back to ELECTRICAL.
        markDirty();
    }

    /** Server-side only: called by the Create output node with the network load utilisation. */
    public void setMechanicalLoad(float utilisation) {
        mechanicalLoad = Mth.clamp(utilisation, 0, 1);
    }

    public void setDumpMode(GasMode mode) {
        if (dumpMode != mode) {
            dumpMode = mode;
            markDirty();
        }
    }

    public void cycleDumpMode() {
        setDumpMode(dumpMode.getNext());
    }

    @ComputerMethod
    public long getMaxProduction() {
        double energyMultiplier = ((double) MekanismConfig.general.maxEnergyPerSteam.get() / MechanicalTurbineValidator.MAX_BLADES)
                                  * (Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
        double rate = lowerVolume * (getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserChemicalFlow.get());
        rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentChemicalFlow.get());
        return MathUtils.clampToLong(energyMultiplier * rate);
    }

    @ComputerMethod
    public long getProductionRate() {
        double energyMultiplier = ((double) MekanismConfig.general.maxEnergyPerSteam.get() / MechanicalTurbineValidator.MAX_BLADES)
                                  * (Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
        return MathUtils.clampToLong(energyMultiplier * clientFlow);
    }

    @ComputerMethod
    public long getMaxFlowRate() {
        double rate = lowerVolume * (getDispersers() * MekanismGeneratorsConfig.generators.turbineDisperserChemicalFlow.get());
        rate = Math.min(rate, vents * MekanismGeneratorsConfig.generators.turbineVentChemicalFlow.get());
        return MathUtils.clampToLong(rate);
    }

    @ComputerMethod
    public long getMaxWaterOutput() {
        return (long) condensers * MekanismGeneratorsConfig.generators.condenserRate.get();
    }

    /** Current mechanical output capacity in Create SU, from the configurable steam-flow ratio. */
    @ComputerMethod
    public long getMechanicalStressCapacity() {
        return Math.max(1, (long) (clientFlow * com.mekcreateturbine.common.config.MCTConfig.MECHANICAL_STRESS_PER_FLOW_RATE.get()));
    }

    public record VentData(BlockPos location, Direction side) {
    }

    // ---- MultiblockData normally exposes protected helpers; MultiblockData has no lists for the
    // output caches in this build, so track them locally like Mek's TurbineMultiblockData does. ----

    private final List<net.neoforged.neoforge.capabilities.BlockCapabilityCache<net.neoforged.neoforge.fluids.capability.IFluidHandler, Direction>> fluidTargets = new ArrayList<>();
    private final List<mekanism.common.integration.energy.BlockEnergyCapabilityCache> energyTargets = new ArrayList<>();

    private List<net.neoforged.neoforge.capabilities.BlockCapabilityCache<net.neoforged.neoforge.fluids.capability.IFluidHandler, Direction>> fluidOutputTargets() {
        return fluidTargets;
    }

    private List<mekanism.common.integration.energy.BlockEnergyCapabilityCache> energyOutputTargets() {
        return energyTargets;
    }
}

package com.mekcreateturbine.common.tile.turbine;

import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.IContentsListener;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.integration.energy.BlockEnergyCapabilityCache;
import com.mekcreateturbine.registration.MCTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Valve block: exposes the multiblock's chemical (steam) tank for intake and the energy
 * container for electrical output.
 */
public class TileEntityMechanicalTurbineValve extends TileEntityMechanicalTurbineCasing {

    private final Map<Direction, BlockEnergyCapabilityCache> energyCapabilityCaches = new EnumMap<>(Direction.class);

    public TileEntityMechanicalTurbineValve(BlockPos pos, BlockState state) {
        super(MCTBlocks.MECHANICAL_TURBINE_VALVE, pos, state);
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        return side -> getMultiblock().getChemicalTanks(side);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        return side -> getMultiblock().getEnergyContainers(side);
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //Do not handle gas/energy when it comes to syncing/saving this tile to disk
        if (type == ContainerType.CHEMICAL || type == ContainerType.ENERGY) {
            return false;
        }
        return super.persists(type);
    }

    public void addEnergyTargetCapability(List<BlockEnergyCapabilityCache> outputTargets, Direction side) {
        BlockEnergyCapabilityCache cache = energyCapabilityCaches.get(side);
        if (cache == null) {
            cache = BlockEnergyCapabilityCache.create((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            energyCapabilityCaches.put(side, cache);
        }
        outputTargets.add(cache);
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }
}

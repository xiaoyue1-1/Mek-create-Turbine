package com.mekcreateturbine.common.content.turbine;

import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData.VentData;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineBearing;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineClutch;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineVent;
import com.mekcreateturbine.registration.MCTBlockTypes;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Cuboid validation for our mechanical turbine. Shell = our casing/valve/vent/clutch; interior
 * reuses Mek components and our bearing shaft.
 *
 * <p>Rules implemented here:
 * <ol>
 *   <li>Block directly above the rotational complex may be an electromagnetic coil OR a bearing
 *       (the old forced-coil check is relaxed).</li>
 *   <li>If that block is a coil, stress output is disabled (center coil = electrical plant).</li>
 *   <li>At least one electromagnetic coil above the disperser plane is required for ELECTRICAL
 *       mode; mechanical does not depend on coils.</li>
 *   <li>Mechanical output needs a roof-centre High-Speed Clutch whose bottom is a contiguous
 *       bearing shaft reaching down to the top of the rotational complex.</li>
 * </ol>
 */
public class MechanicalTurbineValidator extends CuboidStructureValidator<MechanicalTurbineData> {

    public static final int MAX_BLADES = 28;

    public MechanicalTurbineValidator() {
        super(new VoxelCuboid(5, 3, 5), new VoxelCuboid(17, 18, 17));
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MCTBlockTypes.MECHANICAL_TURBINE_CASING)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, MCTBlockTypes.MECHANICAL_TURBINE_VALVE)) {
            return CasingType.VALVE;
        } else if (BlockType.is(block, MCTBlockTypes.MECHANICAL_TURBINE_VENT) || BlockType.is(block, MCTBlockTypes.HIGH_SPEED_CLUTCH)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
        if (super.validateInner(state, chunkMap, pos)) {
            return true;
        }
        // Reused Mek internal components + our bearing shaft:
        return BlockType.is(state.getBlock(), MekanismBlockTypes.PRESSURE_DISPERSER, GeneratorsBlockTypes.TURBINE_ROTOR,
              GeneratorsBlockTypes.ROTATIONAL_COMPLEX, GeneratorsBlockTypes.ELECTROMAGNETIC_COIL, GeneratorsBlockTypes.SATURATING_CONDENSER,
              MCTBlockTypes.MECHANICAL_TURBINE_BEARING);
    }

    @Override
    public FormationResult postcheck(MechanicalTurbineData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
        if (structure.length() % 2 != 1 || structure.width() % 2 != 1) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_EVEN_LENGTH);
        }
        int centerX = structure.getMinPos().getX() + (structure.length() - 1) / 2;
        int centerZ = structure.getMinPos().getZ() + (structure.width() - 1) / 2;

        BlockPos complex = null;

        Set<BlockPos> turbines = new ObjectOpenHashSet<>();
        Set<BlockPos> dispersers = new ObjectOpenHashSet<>();
        Set<BlockPos> coils = new ObjectOpenHashSet<>();
        Set<BlockPos> condensers = new ObjectOpenHashSet<>();

        for (BlockPos pos : structure.internalLocations) {
            BlockEntity tile = WorldUtils.getTileEntity(world, chunkMap, pos);
            if (tile instanceof TileEntityRotationalComplex) {
                if (complex != null || pos.getX() != centerX || pos.getZ() != centerZ) {
                    return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_BAD_COMPLEX, pos);
                }
                complex = pos;
            } else if (tile instanceof TileEntityTurbineRotor) {
                if (pos.getX() != centerX || pos.getZ() != centerZ) {
                    return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_BAD_ROTOR, pos);
                }
                turbines.add(pos);
            } else if (tile instanceof TileEntityPressureDisperser) {
                dispersers.add(pos);
            } else if (tile instanceof TileEntityElectromagneticCoil) {
                coils.add(pos);
            } else if (tile instanceof TileEntitySaturatingCondenser) {
                condensers.add(pos);
            }
        }

        if (complex == null) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MISSING_COMPLEX);
        }

        int rotors = complex.getY() - structure.getMinPos().getY() + 1;
        int innerRadius = (Math.min(structure.length(), structure.width()) - 3) / 2;
        if (innerRadius < rotors / 4) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_TOO_NARROW);
        }
        if (dispersers.isEmpty()) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MISSING_DISPERSERS);
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        final int innerRadiusX = (structure.length() - 3) / 2;
        final int innerRadiusZ = (structure.width() - 3) / 2;
        for (int x = complex.getX() - innerRadiusX; x <= complex.getX() + innerRadiusX; x++) {
            for (int z = complex.getZ() - innerRadiusZ; z <= complex.getZ() + innerRadiusZ; z++) {
                if (x != centerX || z != centerZ) {
                    mutablePos.set(x, complex.getY(), z);
                    TileEntityPressureDisperser tile = WorldUtils.getTileEntity(TileEntityPressureDisperser.class, world, chunkMap, mutablePos);
                    if (tile == null) {
                        return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MISSING_DISPERSER, mutablePos);
                    }
                    dispersers.remove(mutablePos);
                }
            }
        }
        if (!dispersers.isEmpty()) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MALFORMED_DISPERSERS);
        }
        for (BlockPos coord : condensers) {
            if (coord.getY() <= complex.getY()) {
                return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_CONDENSER_BELOW_COMPLEX, coord);
            }
        }
        // Coils must not sit below the disperser plane.
        for (BlockPos coord : coils) {
            if (coord.getY() <= complex.getY()) {
                return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MALFORMED_COILS);
            }
        }
        structure.condensers = condensers.size();
        // Total electromagnetic coils present (used to size electrical conversion; blade count usually caps it).
        structure.coils = coils.size();

        int turbineHeight = 0;
        int blades = 0;
        for (int y = complex.getY() - 1; y > structure.getMinPos().getY(); y--) {
            mutablePos.set(centerX, y, centerZ);
            TileEntityTurbineRotor rotor = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, world, chunkMap, mutablePos);
            if (rotor == null) {
                return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_ROTORS_NOT_CONTIGUOUS);
            }
            turbineHeight++;
            blades += rotor.getHousedBlades();
            turbines.remove(mutablePos);
        }
        if (!turbines.isEmpty()) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_BAD_ROTORS);
        } else if (blades == 0) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_NO_BLADES);
        }
        structure.blades = blades;

        // ---- Determine drivetrain/coil layout above the complex ----
        int topY = structure.getMaxPos().getY();
        mutablePos.set(centerX, complex.getY() + 1, centerZ);
        BlockEntity aboveComplex = WorldUtils.getTileEntity(world, chunkMap, mutablePos);

        boolean hasCoilsAbove = false;
        for (BlockPos p : coils) {
            if (p.getY() > complex.getY()) {
                hasCoilsAbove = true;
                break;
            }
        }
        structure.hasCoils = hasCoilsAbove;

        if (aboveComplex instanceof TileEntityElectromagneticCoil) {
            // Coil on top of the complex -> electrical plant; mechanical drivetrain not possible.
            structure.centerCoil = true;
            structure.mechanicalShaft = false;
        } else if (aboveComplex instanceof TileEntityMechanicalTurbineBearing) {
            structure.centerCoil = false;
            // Walk the bearing column until the clutch on the roof.
            boolean sawBearing = false;
            boolean shaftOk = false;
            for (int y = complex.getY() + 1; y <= topY; y++) {
                mutablePos.set(centerX, y, centerZ);
                BlockEntity tile = WorldUtils.getTileEntity(world, chunkMap, mutablePos);
                if (tile instanceof TileEntityMechanicalTurbineBearing) {
                    sawBearing = true;
                } else if (tile instanceof TileEntityMechanicalTurbineClutch) {
                    // Clutch must sit on top of at least one bearing that connects down to the complex.
                    shaftOk = sawBearing;
                    break;
                } else {
                    break;
                }
            }
            structure.mechanicalShaft = shaftOk;
        } else {
            // Nothing drive-capable sits directly above the complex.
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MISSING_COILS);
        }

        structure.lowerVolume = structure.length() * structure.width() * turbineHeight;
        structure.complex = complex;

        List<VentData> ventData = new ArrayList<>();
        for (BlockPos coord : structure.locations) {
            BlockEntity tile = WorldUtils.getTileEntity(world, chunkMap, coord);
            if (tile instanceof TileEntityMechanicalTurbineVent) {
                if (coord.getY() < complex.getY()) {
                    return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_VENT_BELOW_COMPLEX, coord);
                }
                ventData.add(new VentData(coord, getSide(coord)));
            }
        }
        if (ventData.isEmpty()) {
            return FormationResult.fail(GeneratorsLang.TURBINE_INVALID_MISSING_VENTS);
        }
        structure.updateVentData(ventData);

        // Auto-resolve mode so a fresh structure always starts in an actually-available mode.
        structure.resolveDefaultMode();
        return FormationResult.SUCCESS;
    }
}

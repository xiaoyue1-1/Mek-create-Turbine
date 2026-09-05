package com.mekcreateturbine.common.tile.turbine;

import com.mekcreateturbine.registration.MCTBlocks;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shaft segment ("turbine bearing") used to extend the mechanical drivetrain from the top of the
 * rotational complex up to the high-speed clutch on the roof. It is an interior component like
 * Mek's coils/rotors: inert, gets the formed data back-pointer, and flags a structure recheck when
 * broken.
 */
public class TileEntityMechanicalTurbineBearing extends TileEntityInternalMultiblock {

    public TileEntityMechanicalTurbineBearing(BlockPos pos, BlockState state) {
        super(MCTBlocks.MECHANICAL_TURBINE_BEARING, pos, state);
    }
}

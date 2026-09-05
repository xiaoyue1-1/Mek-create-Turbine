package com.mekcreateturbine.common.tile.turbine;

import com.mekcreateturbine.registration.MCTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * "High-speed clutch": the roof-centre block that terminates a bearing drivetrain and is the
 * mechanical output port. Behaves as an ordinary shell node while formed.
 */
public class TileEntityMechanicalTurbineClutch extends TileEntityMechanicalTurbineCasing {

    public TileEntityMechanicalTurbineClutch(BlockPos pos, BlockState state) {
        this(MCTBlocks.HIGH_SPEED_CLUTCH, pos, state);
    }

    public TileEntityMechanicalTurbineClutch(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }
}

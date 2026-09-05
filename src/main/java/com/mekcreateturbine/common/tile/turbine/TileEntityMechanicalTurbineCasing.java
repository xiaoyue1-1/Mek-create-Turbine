package com.mekcreateturbine.common.tile.turbine;

import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData;
import com.mekcreateturbine.registration.MCTBlocks;
import com.mekcreateturbine.registration.MCTManagers;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Shell casing of our mechanical turbine (edge/wall frame block).
 */
public class TileEntityMechanicalTurbineCasing extends TileEntityMultiblock<MechanicalTurbineData> {

    public TileEntityMechanicalTurbineCasing(BlockPos pos, BlockState state) {
        this(MCTBlocks.MECHANICAL_TURBINE_CASING, pos, state);
    }

    public TileEntityMechanicalTurbineCasing(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @NotNull
    @Override
    public MechanicalTurbineData createMultiblock() {
        return new MechanicalTurbineData(this);
    }

    @Override
    public MultiblockManager<MechanicalTurbineData> getManager() {
        return MCTManagers.MECHANICAL_TURBINE;
    }
}

package com.mekcreateturbine.common.tile.turbine;

import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.IContentsListener;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import com.mekcreateturbine.registration.MCTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Vent block: exposes the multiblock's water output (condenser water) to adjacent fluid pipes.
 */
public class TileEntityMechanicalTurbineVent extends TileEntityMechanicalTurbineCasing {

    private final Map<Direction, BlockCapabilityCache<IFluidHandler, @Nullable Direction>> capabilityCaches = new EnumMap<>(Direction.class);

    public TileEntityMechanicalTurbineVent(BlockPos pos, BlockState state) {
        super(MCTBlocks.MECHANICAL_TURBINE_VENT, pos, state);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //Do not handle fluid when it comes to syncing/saving this tile to disk
        if (type == ContainerType.FLUID) {
            return false;
        }
        return super.persists(type);
    }

    public void addFluidTargetCapability(List<BlockCapabilityCache<IFluidHandler, @Nullable Direction>> outputTargets, Direction side) {
        BlockCapabilityCache<IFluidHandler, @Nullable Direction> cache = capabilityCaches.get(side);
        if (cache == null) {
            cache = Capabilities.FLUID.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            capabilityCaches.put(side, cache);
        }
        outputTargets.add(cache);
    }
}

package com.mekcreateturbine.registration;

import com.mekcreateturbine.MekCreateTurbine;
import com.mekcreateturbine.common.content.mode.OutputMode;
import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

/**
 * Create-side content. Everything here only loads its Create classes when the Create jar is
 * present (guarded at registration time by the loader never touching these classes otherwise is
 * not possible; keep this package unreferenced until Create is confirmed).
 */
public final class MCTCreateContent {

    private MCTCreateContent() {
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MekCreateTurbine.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MekCreateTurbine.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BE_TYPES =
          DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MekCreateTurbine.MODID);

    public static final DeferredBlock<HighSpeedGearboxBlock> HIGH_SPEED_GEARBOX =
          BLOCKS.register("high_speed_gearbox", HighSpeedGearboxBlock::new);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> HIGH_SPEED_GEARBOX_ITEM =
          ITEMS.register("high_speed_gearbox", () -> new net.minecraft.world.item.BlockItem(HIGH_SPEED_GEARBOX.get(),
                new net.minecraft.world.item.Item.Properties()));

    private static BlockEntityType<KineticHighSpeedGearboxTile> gearboxTeType;

    public static final Supplier<BlockEntityType<KineticHighSpeedGearboxTile>> HIGH_SPEED_GEARBOX_TE =
          BE_TYPES.register("high_speed_gearbox", () -> {
              gearboxTeType = BlockEntityType.Builder
                    .of((pos, state) -> new KineticHighSpeedGearboxTile(gearboxTeType, pos, state),
                          HIGH_SPEED_GEARBOX.get()).build(null);
              return gearboxTeType;
          });

    /**
     * "High-Speed Gearbox": the Create rotational output. Place directly on top of a formed
     * turbine's roof-centre high-speed clutch. Publishes SU derived from the turbine's steam flow
     * (config ratio); runs only while the turbine is in MECHANICAL mode.
     */
    public static class HighSpeedGearboxBlock extends Block implements net.minecraft.world.level.block.EntityBlock,
          com.simibubi.create.content.kinetics.base.IRotate {

        public HighSpeedGearboxBlock() {
            super(BlockBehaviour.Properties.of().strength(3.5F, 12.0F).noOcclusion());
        }

        @Override
        public boolean hasShaftTowards(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos,
              BlockState state, net.minecraft.core.Direction direction) {
            // Output on the top face; add further faces after in-game validation of the axis model.
            return direction == net.minecraft.core.Direction.UP;
        }

        @Override
        public net.minecraft.core.Direction.Axis getRotationAxis(BlockState state) {
            return net.minecraft.core.Direction.Axis.Y;
        }

        @Override
        public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
              Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
            return (l, s, st, tile) -> {
                if (tile instanceof KineticHighSpeedGearboxTile gearbox) {
                    gearbox.tick();
                }
            };
        }

        @Override
        public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
            return HIGH_SPEED_GEARBOX_TE.get().create(pos, state);
        }
    }

    public static class KineticHighSpeedGearboxTile extends GeneratingKineticBlockEntity {

        private float activeSpeed;
        private float activeCapacity;
        private boolean running;
        private long lastCap;
        private int timer;
        private int warmup;

        public KineticHighSpeedGearboxTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        private static final float SPEED = 128F;
        private static final int WARMUP_TICKS = 40;

        @Override
        public void tick() {
            super.tick();
            if (level == null || level.isClientSide) {
                return;
            }
            timer++;
            MechanicalTurbineData data = findTurbine();
            boolean wantRunning = data != null && data.isFormed() && data.outputMode == OutputMode.MECHANICAL;
            float capacity = wantRunning ? data.getMechanicalStressCapacity() : 0;
            if (!running) {
                // Delay becoming a Create source until chunk/network initialisation has settled.
                if (!wantRunning || warmup < WARMUP_TICKS) {
                    warmup++;
                    return;
                }
                running = true;
                activeCapacity = capacity;
                lastCap = (long) capacity;
                timer = 0;
                updateGeneratedRotation();
            } else if (timer >= 20 && Math.abs(capacity - lastCap) > lastCap * 0.05F) {
                // Slow refresh of published capacity (only on meaningful change).
                activeCapacity = capacity;
                lastCap = (long) capacity;
                timer = 0;
                updateGeneratedRotation();
            }
            if (!wantRunning) {
                running = false;
                activeCapacity = 0;
                warmup = 0;
                updateGeneratedRotation();
            } else if (data != null) {
                data.setMechanicalLoad(1.0F);
            }
        }

        private MechanicalTurbineData findTurbine() {
            BlockEntity below = level.getBlockEntity(worldPosition.below());
            if (below instanceof TileEntityMechanicalTurbineCasing casing && casing.getMultiblock().isFormed()) {
                return casing.getMultiblock();
            }
            return null;
        }

        @Override
        public float getGeneratedSpeed() {
            return running ? SPEED : 0;
        }

        @Override
        public float calculateAddedStressCapacity() {
            // Create multiplies the capacity base by the rotational speed, so feed base = SU / speed.
            return running ? Math.max(1F, activeCapacity / SPEED) : 0;
        }
    }
}

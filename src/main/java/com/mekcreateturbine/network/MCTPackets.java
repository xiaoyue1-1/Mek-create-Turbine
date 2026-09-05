package com.mekcreateturbine.network;

import com.mekcreateturbine.MekCreateTurbine;
import com.mekcreateturbine.client.gui.GuiMechanicalTurbine;
import com.mekcreateturbine.common.content.mode.OutputMode;
import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MCTPackets {

    private MCTPackets() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(MekCreateTurbine.MODID).versioned("1");
        registrar.playToServer(ToggleMode.TYPE, ToggleMode.CODEC, MCTPackets::handleToggleServer);
        registrar.playToServer(CycleDump.TYPE, CycleDump.CODEC, MCTPackets::handleCycleDumpServer);
        registrar.playToServer(RequestRefresh.TYPE, RequestRefresh.CODEC, MCTPackets::handleRefreshServer);
        registrar.playToClient(OpenTurbine.TYPE, OpenTurbine.CODEC, MCTPackets::handleOpenClient);
    }

    public record ToggleMode(BlockPos pos) implements CustomPacketPayload {

        public static final Type<ToggleMode> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MekCreateTurbine.MODID, "toggle_mode"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleMode> CODEC = StreamCodec.composite(
              BlockPos.STREAM_CODEC, ToggleMode::pos, ToggleMode::new);

        @Override
        public Type<ToggleMode> type() {
            return TYPE;
        }
    }

    public record CycleDump(BlockPos pos) implements CustomPacketPayload {

        public static final Type<CycleDump> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MekCreateTurbine.MODID, "cycle_dump"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CycleDump> CODEC = StreamCodec.composite(
              BlockPos.STREAM_CODEC, CycleDump::pos, CycleDump::new);

        @Override
        public Type<CycleDump> type() {
            return TYPE;
        }
    }

    public record RequestRefresh(BlockPos pos) implements CustomPacketPayload {

        public static final Type<RequestRefresh> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MekCreateTurbine.MODID, "refresh"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestRefresh> CODEC = StreamCodec.composite(
              BlockPos.STREAM_CODEC, RequestRefresh::pos, RequestRefresh::new);

        @Override
        public Type<RequestRefresh> type() {
            return TYPE;
        }
    }

    public record OpenTurbine(BlockPos pos, boolean formed, String mode, String dumpMode, String chemical, long steam, long steamCap, long stressMax,
          long stressCur, boolean canMechanical, boolean canElectrical, long productionFE, long flow, long maxFlow,
          int dispersers, int vents, int blades, int coils, long maxProduction, long maxWater,
          long energyStored, long energyCapacity) implements CustomPacketPayload {

        public static final Type<OpenTurbine> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MekCreateTurbine.MODID, "open_turbine"));

        public static final StreamCodec<RegistryFriendlyByteBuf, OpenTurbine> CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, OpenTurbine p) {
                buf.writeBlockPos(p.pos);
                buf.writeBoolean(p.formed);
                buf.writeUtf(p.mode);
                buf.writeUtf(p.dumpMode);
                buf.writeUtf(p.chemical);
                buf.writeLong(p.steam);
                buf.writeLong(p.steamCap);
                buf.writeLong(p.stressMax);
                buf.writeLong(p.stressCur);
                buf.writeBoolean(p.canMechanical);
                buf.writeBoolean(p.canElectrical);
                buf.writeLong(p.productionFE);
                buf.writeLong(p.flow);
                buf.writeLong(p.maxFlow);
                buf.writeInt(p.dispersers);
                buf.writeInt(p.vents);
                buf.writeInt(p.blades);
                buf.writeInt(p.coils);
                buf.writeLong(p.maxProduction);
                buf.writeLong(p.maxWater);
                buf.writeLong(p.energyStored);
                buf.writeLong(p.energyCapacity);
            }

            @Override
            public OpenTurbine decode(RegistryFriendlyByteBuf buf) {
                return new OpenTurbine(buf.readBlockPos(), buf.readBoolean(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readLong(), buf.readLong(),
                      buf.readLong(), buf.readLong(), buf.readBoolean(), buf.readBoolean(), buf.readLong(), buf.readLong(),
                      buf.readLong(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readLong(), buf.readLong(),
                      buf.readLong(), buf.readLong());
            }
        };

        @Override
        public Type<OpenTurbine> type() {
            return TYPE;
        }
    }

    private static void handleToggleServer(ToggleMode payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.level().getBlockEntity(payload.pos) instanceof TileEntityMechanicalTurbineCasing tile) {
                MechanicalTurbineData data = tile.getMultiblock();
                if (data.isFormed()) {
                    OutputMode next = data.outputMode == OutputMode.ELECTRICAL ? OutputMode.MECHANICAL : OutputMode.ELECTRICAL;
                    if (data.isModeAllowed(next)) {
                        data.setOutputMode(next);
                    }
                }
                sendOpenTurbine(player, tile);
            }
        });
    }

    private static void handleCycleDumpServer(CycleDump payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.level().getBlockEntity(payload.pos) instanceof TileEntityMechanicalTurbineCasing tile) {
                MechanicalTurbineData data = tile.getMultiblock();
                if (data.isFormed()) {
                    data.cycleDumpMode();
                }
                sendOpenTurbine(player, tile);
            }
        });
    }

    private static void handleRefreshServer(RequestRefresh payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.level().getBlockEntity(payload.pos) instanceof TileEntityMechanicalTurbineCasing tile) {
                if (tile.getMultiblock().isFormed()) {
                    sendOpenTurbine(player, tile);
                }
            }
        });
    }

    private static void handleOpenClient(OpenTurbine payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().screen instanceof com.mekcreateturbine.client.gui.GuiMechanicalTurbine gui) {
                // Update live instead of replacing the screen.
                gui.updateData(payload);
            } else {
                net.minecraft.client.Minecraft.getInstance().setScreen(new com.mekcreateturbine.client.gui.GuiMechanicalTurbine(payload));
            }
        });
    }

    public static void sendOpenTurbine(ServerPlayer player, TileEntityMechanicalTurbineCasing tile) {
        MechanicalTurbineData data = tile.getMultiblock();
        if (data.isFormed()) {
            double ratio = com.mekcreateturbine.common.config.MCTConfig.MECHANICAL_STRESS_PER_FLOW_RATE.get();
            long stressMax = (long) (data.getMaxFlowRate() * ratio);
            long stressCur = data.getMechanicalStressCapacity();
            String chemical = "message.mct.chemical.steam";
            PacketDistributor.sendToPlayer(player, new OpenTurbine(tile.getBlockPos(), true, data.outputMode.name(), data.dumpMode.name(), chemical,
                  data.chemicalTank.getStored(), data.chemicalTank.getCapacity(), stressMax, stressCur,
                  data.isModeAllowed(OutputMode.MECHANICAL), data.isModeAllowed(OutputMode.ELECTRICAL),
                  data.getProductionRate(), data.getMaxFlowRate() > 0 ? data.clientFlow : 0, data.getMaxFlowRate(),
                  data.getDispersers(), data.vents, data.blades, data.coils, data.getMaxProduction(), data.getMaxWaterOutput(),
                  data.energyContainer.getEnergy(), data.energyContainer.getMaxEnergy()));
        }
    }
}

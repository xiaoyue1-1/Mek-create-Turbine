package com.mekcreateturbine;

import com.mekcreateturbine.common.content.mode.OutputMode;
import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing;
import com.mekcreateturbine.registration.MCTBlocks;
import com.mekcreateturbine.registration.MCTCreativeTabs;
import com.mekcreateturbine.registration.MCTManagers;
import com.mekcreateturbine.registration.MCTTileEntityTypes;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

/**
 * Mek Create Turbine.
 *
 * <p>Goal: a steam-turbine-shaped multiblock whose shell blocks are ours but whose internals
 * reuse Mekanism blocks (pressure disperser, electromagnetic coil, saturating condenser,
 * rotational complex, turbine rotor/blades, structural glass). Two output modes:
 * ELECTRICAL (like stock) and MECHANICAL (drives a Create rotational source instead of
 * outputting power). Create is an optional (soft) dependency.
 */
@Mod(MekCreateTurbine.MODID)
public class MekCreateTurbine {

    public static final String MODID = "mek_create_turbine";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long TOGGLE_DEBOUNCE_MS = 400;
    private final Map<UUID, Long> lastToggle = new ConcurrentHashMap<>();

    public MekCreateTurbine(IEventBus modBus, net.neoforged.fml.ModContainer modContainer) {
        // Server config for conversion ratios.
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,
              com.mekcreateturbine.common.config.MCTConfig.SPEC);

        // Register content deferred registers to the mod event bus.
        MCTCreativeTabs.TABS.register(modBus);
        MCTBlocks.BLOCKS.register(modBus);
        MCTTileEntityTypes.TILE_ENTITY_TYPES.register(modBus);
        if (CompatHelper.isCreateLoaded()) {
            com.mekcreateturbine.registration.MCTCreateContent.BLOCKS.register(modBus);
            com.mekcreateturbine.registration.MCTCreateContent.ITEMS.register(modBus);
            com.mekcreateturbine.registration.MCTCreateContent.BE_TYPES.register(modBus);
        }
        modBus.addListener(com.mekcreateturbine.network.MCTPackets::register);

        // Initialise the multiblock manager (registers into Mekanism's static manager registry,
        // which powers cache persistence + end-of-tick saving for all managers).
        if (MCTManagers.MECHANICAL_TURBINE == null) {
            throw new IllegalStateException("mechanical turbine manager failed to initialise");
        }

        modBus.addListener(this::commonSetup);
        // Sneak + empty hand on a formed shell block toggles output mode (ELECTRICAL <-> MECHANICAL).
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        LOGGER.info("[{}] loaded. Create present: {}", MODID, CompatHelper.isCreateLoaded());
    }

    private TileEntityMechanicalTurbineCasing resolveTurbine(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TileEntityMechanicalTurbineCasing casing) {
            return casing;
        }
        if (level.getBlockEntity(pos) instanceof mekanism.common.tile.prefab.TileEntityStructuralMultiblock structural) {
            var structure = structural.getStructure(com.mekcreateturbine.registration.MCTManagers.MECHANICAL_TURBINE);
            if (structure != null && structure.getController() instanceof TileEntityMechanicalTurbineCasing casing) {
                return casing;
            }
        }
        return null;
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled()) {
            return;
        }
        Player player = event.getEntity();
        if (player == null || player.isSpectator()) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        TileEntityMechanicalTurbineCasing tile = resolveTurbine(level, pos);
        if (tile == null) {
            return;
        }
        MechanicalTurbineData data = tile.getMultiblock();
        if (!data.isFormed()) {
            return;
        }
        if (!player.isShiftKeyDown()) {
            // Cancel on BOTH sides so a held block isn't placed, and open the panel (server only).
            event.setCanceled(true);
            if (event.getSide().isServer() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                com.mekcreateturbine.network.MCTPackets.sendOpenTurbine(serverPlayer, tile);
            }
            return;
        }
        // Sneak toggle only matters on the server; let the client pass through normally.
        if (event.getSide().isClient()) {
            return;
        }
        if (!player.getMainHandItem().isEmpty()) {
            return;
        }
        // NeoForge can fire RightClickBlock twice for one interaction; debounce per player.
        long now = System.currentTimeMillis();
        Long last = lastToggle.put(player.getUUID(), now);
        if (last != null && now - last < TOGGLE_DEBOUNCE_MS) {
            event.setCanceled(true);
            return;
        }
        OutputMode pre = data.outputMode;
        OutputMode next = pre == OutputMode.ELECTRICAL ? OutputMode.MECHANICAL : OutputMode.ELECTRICAL;
        if (!data.isModeAllowed(next)) {
            Component why = next == OutputMode.MECHANICAL
                  ? Component.literal("应力模式不可用：复合体上方为线圈/缺轴承传动+离合")
                  : Component.literal("电力模式不可用：分压元件上方缺少电磁线圈");
            player.displayClientMessage(why, true);
            event.setCanceled(true);
            return;
        }
        data.setOutputMode(next);
        LOGGER.info("[mct] toggle @{} pre={} set={} after={} hash={}", pos, pre, next, data.outputMode,
              System.identityHashCode(data));
        player.displayClientMessage(Component.literal("Output mode: ")
              .append(Component.literal(next.name()).withStyle(next == OutputMode.MECHANICAL ? ChatFormatting.AQUA : ChatFormatting.GOLD)), true);
        event.setCanceled(true);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Cross-mod capability / integration wiring goes here (Create output side).
    }
}

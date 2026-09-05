package com.mekcreateturbine.client;

import com.mekcreateturbine.MekCreateTurbine;
import com.mekcreateturbine.client.render.RenderMechanicalTurbine;
import com.mekcreateturbine.registration.MCTTileEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = MekCreateTurbine.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MCTClientEvents {

    private MCTClientEvents() {
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MCTTileEntityTypes.MECHANICAL_TURBINE_CASING.get(), RenderMechanicalTurbine::new);
        event.registerBlockEntityRenderer(MCTTileEntityTypes.MECHANICAL_TURBINE_VALVE.get(), RenderMechanicalTurbine::new);
        event.registerBlockEntityRenderer(MCTTileEntityTypes.MECHANICAL_TURBINE_VENT.get(), RenderMechanicalTurbine::new);
        event.registerBlockEntityRenderer(MCTTileEntityTypes.HIGH_SPEED_CLUTCH.get(), RenderMechanicalTurbine::new);
    }
}

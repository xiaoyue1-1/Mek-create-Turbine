package com.mekcreateturbine.registration;

import com.mekcreateturbine.MekCreateTurbine;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Central creative-mode tab. Content (blocks/items) will be added here as the turbine
 * shell and output blocks are implemented.
 */
public final class MCTCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
          DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MekCreateTurbine.MODID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
          .title(Component.translatable("itemGroup." + MekCreateTurbine.MODID))
          .icon(() -> ItemStack.EMPTY)
          .displayItems((params, output) -> {
              output.accept(MCTBlocks.MECHANICAL_TURBINE_CASING.get());
              output.accept(MCTBlocks.MECHANICAL_TURBINE_VALVE.get());
              output.accept(MCTBlocks.MECHANICAL_TURBINE_VENT.get());
              output.accept(MCTBlocks.MECHANICAL_TURBINE_BEARING.get());
              output.accept(MCTBlocks.HIGH_SPEED_CLUTCH.get());
              if (com.mekcreateturbine.CompatHelper.isCreateLoaded()) {
                  output.accept(com.mekcreateturbine.registration.MCTCreateContent.HIGH_SPEED_GEARBOX.get());
              }
          })
          .build());

    private MCTCreativeTabs() {
    }
}

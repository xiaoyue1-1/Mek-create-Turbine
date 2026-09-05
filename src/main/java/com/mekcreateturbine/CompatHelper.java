package com.mekcreateturbine;

import net.neoforged.fml.ModList;

/**
 * Soft-dependency helpers. All code that touches Create classes MUST go through
 * {@link #isCreateLoaded()} and live in classes only loaded while Create is present,
 * otherwise servers without Create will crash on class init.
 */
public final class CompatHelper {

    private CompatHelper() {
    }

    public static boolean isCreateLoaded() {
        return ModList.get() != null && ModList.get().isLoaded("create");
    }

    public static boolean isMekanismLoaded() {
        return ModList.get() != null && ModList.get().isLoaded("mekanism");
    }
}

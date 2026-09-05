package com.mekcreateturbine.registration;

import com.mekcreateturbine.common.content.turbine.MechanicalTurbineCache;
import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData;
import com.mekcreateturbine.common.content.turbine.MechanicalTurbineValidator;
import mekanism.common.lib.multiblock.MultiblockManager;

/**
 * Multiblock managers for this mod.
 *
 * <p>Important: manager name must NOT contain "reactor" so reused Mek structural glass can join
 * the structure (TileEntityStructuralGlass.canInterface). Registered instances are picked up by
 * Mekanism's own {@code MultiblockManager} static registry + saved-data handling automatically.
 */
public final class MCTManagers {

    public static final MultiblockManager<MechanicalTurbineData> MECHANICAL_TURBINE =
          new MultiblockManager<>("mctMechanicalTurbine", MechanicalTurbineCache::new, MechanicalTurbineValidator::new);

    private MCTManagers() {
    }
}

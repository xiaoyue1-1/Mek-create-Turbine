package com.mekcreateturbine.integration.jade;

import com.mekcreateturbine.MekCreateTurbine;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade (Waila) integration: shows live turbine info (steam, max/actual stress) when looking at
 * the formed turbine shell blocks. Live numbers are gathered server-side and sent per-target.
 */
@WailaPlugin(MekCreateTurbine.MODID)
public class MCTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new TurbineDataProvider(),
              com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new TurbineTooltipProvider(),
              mekanism.common.block.prefab.BlockBasicMultiblock.class);
    }
}

package com.mekcreateturbine.registration;

import com.mekcreateturbine.MekCreateTurbine;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineBearing;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineClutch;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineValve;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineVent;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.world.level.material.MapColor;

/**
 * Shell blocks for our mechanical turbine (mirror of GeneratorsBlocks turbine entries). The
 * rotor/complex/coil/condenser/disperser/glass are reused from Mekanism, so they are NOT
 * registered here.
 */
public final class MCTBlocks {

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekCreateTurbine.MODID);

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityMechanicalTurbineCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityMechanicalTurbineCasing>>> MECHANICAL_TURBINE_CASING =
          BLOCKS.registerDetails("mechanical_turbine_casing",
                () -> new BlockBasicMultiblock<>(MCTBlockTypes.MECHANICAL_TURBINE_CASING, properties -> properties.mapColor(MapColor.CLAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityMechanicalTurbineValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityMechanicalTurbineValve>>> MECHANICAL_TURBINE_VALVE =
          BLOCKS.registerDetails("mechanical_turbine_valve",
                () -> new BlockBasicMultiblock<>(MCTBlockTypes.MECHANICAL_TURBINE_VALVE, properties -> properties.mapColor(MapColor.CLAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityMechanicalTurbineVent>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityMechanicalTurbineVent>>> MECHANICAL_TURBINE_VENT =
          BLOCKS.registerDetails("mechanical_turbine_vent",
                () -> new BlockBasicMultiblock<>(MCTBlockTypes.MECHANICAL_TURBINE_VENT, properties -> properties.mapColor(MapColor.COLOR_GRAY)));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityMechanicalTurbineBearing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityMechanicalTurbineBearing>>> MECHANICAL_TURBINE_BEARING =
          BLOCKS.registerDetails("turbine_bearing",
                () -> new BlockBasicMultiblock<>(MCTBlockTypes.MECHANICAL_TURBINE_BEARING,
                      properties -> properties.mapColor(MapColor.COLOR_GRAY).noOcclusion()));
    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityMechanicalTurbineClutch>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityMechanicalTurbineClutch>>> HIGH_SPEED_CLUTCH =
          BLOCKS.registerDetails("high_speed_clutch",
                () -> new BlockBasicMultiblock<>(MCTBlockTypes.HIGH_SPEED_CLUTCH, properties -> properties.mapColor(MapColor.COLOR_GRAY)));

    private MCTBlocks() {
    }
}

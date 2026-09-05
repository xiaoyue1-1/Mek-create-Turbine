package com.mekcreateturbine.registration;

import com.mekcreateturbine.MekCreateTurbine;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineBearing;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineClutch;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineValve;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineVent;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;

/**
 * Block entity types for our turbine shell blocks (mirror of GeneratorsTileEntityTypes).
 */
public final class MCTTileEntityTypes {

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(MekCreateTurbine.MODID);

    public static final TileEntityTypeRegistryObject<TileEntityMechanicalTurbineCasing> MECHANICAL_TURBINE_CASING = TILE_ENTITY_TYPES
          .mekBuilder(MCTBlocks.MECHANICAL_TURBINE_CASING, TileEntityMechanicalTurbineCasing::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalTurbineValve> MECHANICAL_TURBINE_VALVE = TILE_ENTITY_TYPES
          .mekBuilder(MCTBlocks.MECHANICAL_TURBINE_VALVE, TileEntityMechanicalTurbineValve::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalTurbineVent> MECHANICAL_TURBINE_VENT = TILE_ENTITY_TYPES
          .mekBuilder(MCTBlocks.MECHANICAL_TURBINE_VENT, TileEntityMechanicalTurbineVent::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalTurbineBearing> MECHANICAL_TURBINE_BEARING = TILE_ENTITY_TYPES
          .mekBuilder(MCTBlocks.MECHANICAL_TURBINE_BEARING, TileEntityMechanicalTurbineBearing::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();
    public static final TileEntityTypeRegistryObject<TileEntityMechanicalTurbineClutch> HIGH_SPEED_CLUTCH = TILE_ENTITY_TYPES
          .mekBuilder(MCTBlocks.HIGH_SPEED_CLUTCH, TileEntityMechanicalTurbineClutch::new)
          .clientTicker(TileEntityMekanism::tickClient)
          .serverTicker(TileEntityMekanism::tickServer)
          .build();

    private MCTTileEntityTypes() {
    }
}

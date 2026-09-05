package com.mekcreateturbine.registration;

import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineBearing;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineClutch;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineValve;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineVent;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder;
import mekanism.generators.common.GeneratorsLang;

/**
 * Block types (block + tile pair definitions) for the shell blocks.
 *
 * <p>TODO(lang): descriptions currently reuse Mek's GeneratorsLang entries for parity; replace
 * with own lang. TODO(gui): add a mode-switch GUI / container later.
 */
public final class MCTBlockTypes {

    public static final BlockTypeTile<TileEntityMechanicalTurbineCasing> MECHANICAL_TURBINE_CASING = BlockTileBuilder
          .createBlock(() -> MCTTileEntityTypes.MECHANICAL_TURBINE_CASING, GeneratorsLang.DESCRIPTION_TURBINE_CASING)
          .build();
    public static final BlockTypeTile<TileEntityMechanicalTurbineValve> MECHANICAL_TURBINE_VALVE = BlockTileBuilder
          .createBlock(() -> MCTTileEntityTypes.MECHANICAL_TURBINE_VALVE, GeneratorsLang.DESCRIPTION_TURBINE_VALVE)
          .build();
    public static final BlockTypeTile<TileEntityMechanicalTurbineVent> MECHANICAL_TURBINE_VENT = BlockTileBuilder
          .createBlock(() -> MCTTileEntityTypes.MECHANICAL_TURBINE_VENT, GeneratorsLang.DESCRIPTION_TURBINE_VENT)
          .build();
    public static final BlockTypeTile<TileEntityMechanicalTurbineBearing> MECHANICAL_TURBINE_BEARING = BlockTileBuilder
          .createBlock(() -> MCTTileEntityTypes.MECHANICAL_TURBINE_BEARING, GeneratorsLang.DESCRIPTION_TURBINE_ROTOR)
          .build();
    public static final BlockTypeTile<TileEntityMechanicalTurbineClutch> HIGH_SPEED_CLUTCH = BlockTileBuilder
          .createBlock(() -> MCTTileEntityTypes.HIGH_SPEED_CLUTCH, GeneratorsLang.DESCRIPTION_TURBINE_CASING)
          .build();

    private MCTBlockTypes() {
    }
}

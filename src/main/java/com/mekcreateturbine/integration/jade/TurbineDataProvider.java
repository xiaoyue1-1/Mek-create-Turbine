package com.mekcreateturbine.integration.jade;

import com.mekcreateturbine.MekCreateTurbine;
import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Pushes the turbine's live values to Jade's per-target server data.
 */
public class TurbineDataProvider implements IServerDataProvider<BlockAccessor> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MekCreateTurbine.MODID, "turbine_data");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof TileEntityMechanicalTurbineCasing casing)) {
            return;
        }
        MechanicalTurbineData mb = casing.getMultiblock();
        if (!mb.isFormed()) {
            data.putBoolean("formed", false);
            return;
        }
        data.putBoolean("formed", true);
        data.putString("mode", mb.outputMode.name());
        data.putLong("steam", mb.chemicalTank.getStored());
        data.putLong("steamCap", mb.chemicalTank.getCapacity());
        double ratio = com.mekcreateturbine.common.config.MCTConfig.MECHANICAL_STRESS_PER_FLOW_RATE.get();
        data.putLong("stressMax", (long) (mb.getMaxFlowRate() * ratio));
        data.putLong("stressCur", mb.getMechanicalStressCapacity());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}

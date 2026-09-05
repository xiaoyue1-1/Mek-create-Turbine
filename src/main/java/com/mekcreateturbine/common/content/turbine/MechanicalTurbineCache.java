package com.mekcreateturbine.common.content.turbine;

import com.mekcreateturbine.common.content.mode.OutputMode;
import mekanism.common.lib.multiblock.MultiblockCache;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * Persists per-multiblock data across disassembly/reassembly (mirror of Mek's TurbineCache),
 * extended to remember the {@link OutputMode} of the turbine.
 */
public class MechanicalTurbineCache extends MultiblockCache<MechanicalTurbineData> {

    private OutputMode outputMode = OutputMode.ELECTRICAL;

    @Override
    public void merge(MultiblockCache<MechanicalTurbineData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        if (mergeCache instanceof MechanicalTurbineCache other) {
            outputMode = other.outputMode;
        }
    }

    @Override
    public void apply(HolderLookup.Provider provider, MechanicalTurbineData data) {
        super.apply(provider, data);
        data.outputMode = outputMode;
    }

    @Override
    public void sync(MechanicalTurbineData data) {
        super.sync(data);
        outputMode = data.outputMode;
    }

    @Override
    public void load(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.load(provider, nbtTags);
        String stored = nbtTags.getString("OutputMode");
        for (OutputMode mode : OutputMode.values()) {
            if (mode.name().equals(stored)) {
                outputMode = mode;
                break;
            }
        }
    }

    @Override
    public void save(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.save(provider, nbtTags);
        nbtTags.putString("OutputMode", outputMode.name());
    }
}

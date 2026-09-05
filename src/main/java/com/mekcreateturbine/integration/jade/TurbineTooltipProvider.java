package com.mekcreateturbine.integration.jade;

import com.mekcreateturbine.MekCreateTurbine;
import com.mekcreateturbine.common.content.mode.OutputMode;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Client-side tooltip lines for the formed turbine (localised).
 */
public class TurbineTooltipProvider implements IBlockComponentProvider {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MekCreateTurbine.MODID, "turbine_info");
    private static final String M = "message.mct.jade.";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data == null || !data.contains("formed")) {
            return;
        }
        if (!data.getBoolean("formed")) {
            tooltip.add(Component.translatable(M + "unformed").withStyle(ChatFormatting.GRAY));
            return;
        }
        String mode = data.getString("mode");
        ChatFormatting color = "MECHANICAL".equals(mode) ? ChatFormatting.AQUA : ChatFormatting.GOLD;
        tooltip.add(Component.translatable(M + "mode").append(": ")
              .append(Component.translatable("message.mct.mode." + mode.toLowerCase())).withStyle(color));
        tooltip.add(Component.translatable(M + "steam").append(": " + data.getLong("steam") + " / " + data.getLong("steamCap") + " mB"));
        tooltip.add(Component.translatable(M + "stressmax").append(": " + data.getLong("stressMax") + " SU"));
        tooltip.add(Component.translatable(M + "stresscur").append(": " + data.getLong("stressCur") + " SU"));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}

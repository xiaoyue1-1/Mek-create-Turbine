package com.mekcreateturbine.client.render;

import com.mekcreateturbine.common.content.turbine.MechanicalTurbineData;
import com.mekcreateturbine.common.tile.turbine.TileEntityMechanicalTurbineCasing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.util.WorldUtils;
import mekanism.generators.client.render.RenderTurbineRotor;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

/**
 * Master renderer for our mechanical turbine: draws the spinning rotor blades and the interior
 * steam fill once formed (mirror of Mek's RenderIndustrialTurbine).
 */
@NothingNullByDefault
public class RenderMechanicalTurbine extends MultiblockTileEntityRenderer<MechanicalTurbineData, TileEntityMechanicalTurbineCasing> {

    public RenderMechanicalTurbine(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityMechanicalTurbineCasing tile, MechanicalTurbineData multiblock, float partialTick, PoseStack matrix,
          MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        BlockPos pos = tile.getBlockPos();
        profiler.push("mechanicalTurbineRotor");
        if (multiblock.complex != null && RenderTurbineRotor.INSTANCE != null) {
            // Make sure the static client rotation table is populated so the rotor renderer spins.
            if (multiblock.inventoryID != null) {
                TurbineMultiblockData.clientRotationMap.put(multiblock.inventoryID, multiblock.clientRotation);
            }
            BlockPos.MutableBlockPos complexPos = new BlockPos.MutableBlockPos(multiblock.complex.getX(), multiblock.complex.getY(), multiblock.complex.getZ());
            VertexConsumer buffer = RenderTurbineRotor.INSTANCE.getBuffer(renderer);
            matrix.pushPose();
            matrix.translate(complexPos.getX() - pos.getX(), complexPos.getY() - pos.getY(), complexPos.getZ() - pos.getZ());
            while (true) {
                complexPos.move(Direction.DOWN);
                TileEntityTurbineRotor rotor = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, tile.getLevel(), complexPos);
                if (rotor == null) {
                    break;
                }
                if (rotor.getMultiblockUUID() == null && rotor instanceof IInternalMultiblock internal) {
                    // Backfill the formed-data pointer so the rotor carries the multiblock uuid.
                    internal.setMultiblock(multiblock);
                }
                matrix.translate(0, -1, 0);
                RenderTurbineRotor.INSTANCE.render(rotor, matrix, buffer, LightTexture.FULL_SKY, overlayLight);
            }
            matrix.popPose();
        }
        profiler.pop();
        if (!multiblock.chemicalTank.isEmpty() && multiblock.length() > 0) {
            int height = multiblock.lowerVolume / (multiblock.length() * multiblock.width());
            if (height > 0) {
                RenderData data = RenderData.Builder.create(multiblock.chemicalTank.getStack())
                      .of(multiblock)
                      .height(height)
                      .build();
                renderObject(data, pos, matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()), overlayLight, multiblock.prevSteamScale);
            }
        }
    }

    @Override
    protected boolean shouldRender(TileEntityMechanicalTurbineCasing tile, MechanicalTurbineData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && multiblock.complex != null;
    }

    @Override
    protected String getProfilerSection() {
        return "mechanicalTurbine";
    }
}

package com.mekcreateturbine.client.gui;

import com.mekcreateturbine.common.content.mode.OutputMode;
import com.mekcreateturbine.network.MCTPackets;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Two-page turbine panel, vanilla inventory style, small shadow-free text, localised labels.
 */
public class GuiMechanicalTurbine extends Screen {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 150;
    private static final float TEXT_SCALE = 0.8F;
    private static final String M = "message.mct.";

    private MCTPackets.OpenTurbine data;
    private int page;
    private int refreshTimer;
    private Button dumpButton;
    private Button switchButton;

    public GuiMechanicalTurbine(MCTPackets.OpenTurbine data) {
        super(Component.translatable(M + "title"));
        this.data = data;
    }

    @Override
    protected void init() {
        super.init();
        int left = (width - WIDTH) / 2;
        int top = (height - HEIGHT) / 2;
        addRenderableWidget(Button.builder(Component.empty(), b -> page = 0)
              .bounds(left - 22, top + 2, 17, 17).build());
        addRenderableWidget(Button.builder(Component.empty(), b -> page = 1)
              .bounds(left - 22, top + 22, 17, 17).build());
        boolean currentlyMechanical = OutputMode.MECHANICAL.name().equals(data.mode());
        boolean canSwitch = currentlyMechanical ? data.canElectrical() : data.canMechanical();
        switchButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
            if (canSwitch) {
                PacketDistributor.sendToServer(new MCTPackets.ToggleMode(data.pos()));
            }
        }).bounds(left + 112, top + 96, 56, 12).build());
        dumpButton = addRenderableWidget(Button.builder(Component.empty(), b ->
              PacketDistributor.sendToServer(new MCTPackets.CycleDump(data.pos()))
        ).bounds(left + 8, top + 96, 100, 12).build());
    }

    public void updateData(MCTPackets.OpenTurbine payload) {
        this.data = payload;
    }

    @Override
    public void tick() {
        super.tick();
        refreshTimer++;
        if (refreshTimer >= 15) {
            refreshTimer = 0;
            PacketDistributor.sendToServer(new MCTPackets.RequestRefresh(data.pos()));
        }
    }

    private String dumpKey(String mode) {
        return switch (mode) {
            case "DUMPING" -> M + "dump.dump";
            case "DUMPING_EXCESS" -> M + "dump.excess";
            default -> M + "dump.idle";
        };
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        super.render(g, mx, my, partial);
        int left = (width - WIDTH) / 2;
        int top = (height - HEIGHT) / 2;
        drawTabIcon(g, left - 22, top + 2, "i");
        drawTabIcon(g, left - 22, top + 22, "◆");
        if (page == 0) {
            renderOverview(g, left, top, mx, my);
        } else {
            renderData(g, left, top);
        }
        boolean overview = page == 0;
        switchButton.visible = overview;
        dumpButton.visible = overview;
        if (overview) {
            drawButtonFrame(g, switchButton);
            drawButtonFrame(g, dumpButton);
            boolean currentlyMechanical = OutputMode.MECHANICAL.name().equals(data.mode());
            boolean canSwitch = currentlyMechanical ? data.canElectrical() : data.canMechanical();
            drawButtonText(g, switchButton, Component.translatable(canSwitch ? M + "button.switch" : M + "button.only"));
            drawButtonText(g, dumpButton, Component.translatable(M + "button.dump").append(": ")
                  .append(Component.translatable(dumpKey(data.dumpMode()))));
        }
    }

    private void drawTabIcon(GuiGraphics g, int x, int y, String icon) {
        drawBevel(g, x, y, 17, 17);
        g.drawCenteredString(font, Component.literal(icon), x + 8, y + 5, 0xFF404040);
    }

    private void renderOverview(GuiGraphics g, int left, int top, int mx, int my) {
        long steam = data.steam();
        long cap = data.steamCap();
        drawBevel(g, left, top, WIDTH, HEIGHT);
        smallC(g, left + WIDTH / 2, top + 8, Component.translatable(M + "title.overview"));

        int gx = left + 12;
        int gy = top + 24;
        int gh = 46;
        fillSlot(g, gx, gy, 12, gh);
        if (cap > 0) {
            int fill = (int) (gh * steam / cap);
            g.fill(gx + 1, gy + gh - fill, gx + 11, gy + gh, 0xFF3FB6E8);
        }
        if (mx >= gx && mx < gx + 12 && my >= gy && my < gy + gh) {
            g.renderTooltip(font, Component.literal(steam + " mB").append(" (").append(Component.translatable(data.chemical())).append(")"), mx, my);
        }

        fillSlot(g, gx + 14, gy, 12, gh);
        long energyStored = data.energyStored();
        long energyCap = data.energyCapacity();
        if (energyCap > 0) {
            int fill = (int) (gh * Math.min(1.0, (double) energyStored / energyCap));
            g.fill(gx + 15, gy + gh - fill, gx + 25, gy + gh, 0xFF55C855);
        }
        if (mx >= gx + 14 && mx < gx + 26 && my >= gy && my < gy + gh) {
            g.renderTooltip(font, Component.literal(human(energyStored) + " FE / " + human(energyCap) + " FE"), mx, my);
        }

        int tx = left + 42;
        int ty = top + 24;
        OutputMode mode = OutputMode.MECHANICAL.name().equals(data.mode()) ? OutputMode.MECHANICAL : OutputMode.ELECTRICAL;
        small(g, tx, ty, Component.translatable(M + "stat.output").append(": " + human(data.productionFE()) + " FE").withStyle(ChatFormatting.GREEN));
        small(g, tx, ty + 10, Component.translatable(M + "stat.flow").append(": " + human(data.flow()) + " mB/t").withStyle(ChatFormatting.GREEN));
        small(g, tx, ty + 20, Component.translatable(M + "stat.capacity").append(": " + human(cap) + " mB"));
        small(g, tx, ty + 30, Component.translatable(M + "stat.maxflow").append(": " + human(data.maxFlow()) + " mB/t"));
        small(g, tx, ty + 42, Component.translatable(M + "stat.mode").append(": ")
              .append(Component.translatable(M + "mode." + mode.name().toLowerCase()))
              .withStyle(mode == OutputMode.MECHANICAL ? ChatFormatting.AQUA : ChatFormatting.GOLD));
        small(g, tx, ty + 52, Component.translatable(M + "stat.stressmax").append(": " + human(data.stressMax()) + " SU"));
        small(g, tx, ty + 64, Component.translatable(M + "stat.stresscur").append(": " + human(data.stressCur()) + " SU"));
    }

    private void renderData(GuiGraphics g, int left, int top) {
        drawBevel(g, left, top, WIDTH, HEIGHT);
        smallC(g, left + WIDTH / 2, top + 8, Component.translatable(M + "title.data"));
        int tx = left + 12;
        int ty = top + 26;
        small(g, tx, ty, Component.translatable(M + "data.tank").append(": " + human(data.steamCap())));
        ty += 14;
        small(g, tx, ty, Component.translatable(M + "data.steamprocessing"));
        ty += 11;
        small(g, tx + 4, ty, Component.translatable(M + "data.dispersers").append(": " + data.dispersers()));
        ty += 11;
        small(g, tx + 4, ty, Component.translatable(M + "data.vents").append(": " + data.vents()));
        ty += 14;
        small(g, tx, ty, Component.translatable(M + "data.output"));
        ty += 11;
        small(g, tx + 4, ty, Component.translatable(M + "data.blades").append(": " + data.blades()));
        ty += 11;
        small(g, tx + 4, ty, Component.translatable(M + "data.coils").append(": " + data.coils()));
        ty += 16;
        small(g, tx, ty, Component.translatable(M + "data.maxoutput").append(": " + human(data.maxProduction()) + " FE"));
        ty += 11;
        small(g, tx, ty, Component.translatable(M + "data.maxwater").append(": " + human(data.maxWater()) + " mB/t"));
    }

    private String human(long v) {
        if (v >= 1_000_000_000_000L) {
            return trim(v / 1_000_000_000_000.0) + "T";
        }
        if (v >= 1_000_000_000L) {
            return trim(v / 1_000_000_000.0) + "G";
        }
        if (v >= 1_000_000L) {
            return trim(v / 1_000_000.0) + "M";
        }
        if (v >= 1_000L) {
            return trim(v / 1_000.0) + "K";
        }
        return Long.toString(v);
    }

    private static String trim(double d) {
        return String.format("%.2f", d);
    }

    private void small(GuiGraphics g, int x, int y, Component text) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(TEXT_SCALE, TEXT_SCALE, 1);
        g.drawString(font, text, 0, 0, 0xFF404040, false);
        g.pose().popPose();
    }

    private void smallC(GuiGraphics g, int x, int y, Component text) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(TEXT_SCALE, TEXT_SCALE, 1);
        g.drawString(font, text, -font.width(text) / 2, 0, 0xFF404040, false);
        g.pose().popPose();
    }

    private void drawButtonText(GuiGraphics g, Button b, Component text) {
        int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();
        g.pose().pushPose();
        g.pose().translate(x + w / 2.0F, y + h / 2.0F, 0);
        g.pose().scale(TEXT_SCALE, TEXT_SCALE, 1);
        g.drawString(font, text, -font.width(text) / 2, -4, 0xFF404040, false);
        g.pose().popPose();
    }

    private void drawButtonFrame(GuiGraphics g, Button b) {
        int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();
        g.fill(x, y, x + w, y + h, 0xFF8B8B8B);
        g.fill(x, y, x + w, y + 1, 0xFFFFFFFF);
        g.fill(x, y, x + 1, y + h, 0xFFFFFFFF);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF373737);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF373737);
    }

    private void drawBevel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFFC6C6C6);
        g.fill(x, y, x + w, y + 1, 0xFFFFFFFF);
        g.fill(x, y, x + 1, y + h, 0xFFFFFFFF);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF555555);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF555555);
    }

    private void fillSlot(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0xFF8B8B8B);
        g.fill(x, y, x + w, y + 1, 0xFFFFFFFF);
        g.fill(x, y, x + 1, y + h, 0xFFFFFFFF);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF373737);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF373737);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

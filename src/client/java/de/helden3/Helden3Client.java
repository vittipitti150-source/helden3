package de.helden3;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class Helden3Client implements ClientModInitializer {

    private static final String[] HEART = {
            " XX   XX ",
            "XXXX XXXX",
            "XXXXXXXXX",
            "XXXXXXXXX",
            "XXXXXXXXX",
            " XXXXXXX ",
            "  XXXXX  ",
            "   XXX   ",
            "    X    "
    };

    private static final int SCALE = 2;
    private static final int SPACING = 4;
    private static final int MARGIN = 6;

    private static final int COLOR_FULL = 0xFF2F6BFF;
    private static final int COLOR_HIGHLIGHT = 0xFFBBD4FF;
    private static final int COLOR_LOST = 0xFF3A3A3A;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(Helden3.MOD_ID, "lives"),
                Helden3Client::extract
        );
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        int lives = mc.player.getAttachedOrElse(Helden3.LIVES, Helden3.MAX_LIVES);
        int heartSize = 9 * SCALE;
        int y = mc.getWindow().getGuiScaledHeight() - heartSize - MARGIN;

        for (int i = 0; i < Helden3.MAX_LIVES; i++) {
            int x = MARGIN + i * (heartSize + SPACING);
            boolean full = i < lives;
            drawHeart(graphics, x, y, full ? COLOR_FULL : COLOR_LOST, full);
        }
    }

    private static void drawHeart(GuiGraphicsExtractor graphics, int x, int y, int color, boolean highlight) {
        for (int row = 0; row < HEART.length; row++) {
            for (int col = 0; col < HEART[row].length(); col++) {
                if (HEART[row].charAt(col) == 'X') {
                    int px = x + col * SCALE;
                    int py = y + row * SCALE;
                    graphics.fill(px, py, px + SCALE, py + SCALE, color);
                }
            }
        }
        if (highlight) {
            graphics.fill(x + SCALE, y + SCALE, x + 2 * SCALE, y + 2 * SCALE, COLOR_HIGHLIGHT);
        }
    }
}

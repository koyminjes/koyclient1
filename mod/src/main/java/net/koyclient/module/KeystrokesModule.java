package net.koyclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;

public class KeystrokesModule extends KoyModule {
    private static final int K = 22; // key size
    private static final int G = 2;  // gap

    public KeystrokesModule() {
        super("keystrokes", "Keystrokes", "Shows WASD, space and mouse buttons.", true, 4, 72);
    }

    @Override
    public int getWidth() {
        return K * 3 + G * 2;
    }

    @Override
    public int getHeight() {
        return (K + G) * 3 + 14;
    }

    @Override
    protected void render(DrawContext ctx, TextRenderer font) {
        GameOptions o = MinecraftClient.getInstance().options;
        int step = K + G;
        int half = (getWidth() - G) / 2;

        key(ctx, font, "W", step, 0, K, K, o.forwardKey.isPressed());
        key(ctx, font, "A", 0, step, K, K, o.leftKey.isPressed());
        key(ctx, font, "S", step, step, K, K, o.backKey.isPressed());
        key(ctx, font, "D", step * 2, step, K, K, o.rightKey.isPressed());
        key(ctx, font, "LMB", 0, step * 2, half, K, ClickTracker.leftDown());
        key(ctx, font, "RMB", half + G, step * 2, half, K, ClickTracker.rightDown());
        key(ctx, font, "SPACE", 0, step * 3, getWidth(), 14, o.jumpKey.isPressed());
    }

    private void key(DrawContext ctx, TextRenderer font, String label, int x, int y, int w, int h, boolean down) {
        ctx.fill(x, y, x + w, y + h, down ? 0xAAFFFFFF : 0x66000000);
        int tx = x + (w - font.getWidth(label)) / 2;
        int ty = y + (h - 8) / 2;
        if (down) {
            ctx.drawText(font, label, tx, ty, 0xFF000000, false);
        } else {
            ctx.drawTextWithShadow(font, label, tx, ty, 0xFFFFFFFF);
        }
    }
}

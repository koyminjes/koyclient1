package net.koyclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** A small translucent box with one line of text. */
public abstract class TextModule extends KoyModule {
    protected TextModule(String id, String name, String description, boolean enabled, int x, int y) {
        super(id, name, description, enabled, x, y);
    }

    protected abstract String text();

    @Override
    public int getWidth() {
        return MinecraftClient.getInstance().textRenderer.getWidth(text()) + 6;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    protected void render(DrawContext ctx, TextRenderer font) {
        ctx.fill(0, 0, getWidth(), getHeight(), 0x66000000);
        ctx.drawTextWithShadow(font, text(), 3, 3, 0xFFFFFFFF);
    }
}

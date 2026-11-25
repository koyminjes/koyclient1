package net.koyclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public abstract class KoyModule {
    private final String id;
    private final String name;
    private final String description;
    private boolean enabled;
    private int x, y;
    private float scale = 1f;

    protected KoyModule(String id, String name, String description, boolean enabled, int x, int y) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.x = x;
        this.y = y;
    }

    /** False for modules with no on-screen element (sprint, zoom). */
    public boolean isHud() {
        return true;
    }

    public void tick(MinecraftClient mc) {}

    public abstract int getWidth();

    public abstract int getHeight();

    /** Draw at (0,0); position and scale are applied by renderScaled. */
    protected abstract void render(DrawContext ctx, TextRenderer font);

    public final void renderScaled(DrawContext ctx) {
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate((float) x, (float) y, 0f);
        m.scale(scale, scale, 1f);
        render(ctx, MinecraftClient.getInstance().textRenderer);
        m.pop();
    }

    public boolean contains(double mx, double my) {
        return mx >= x && mx <= x + getWidth() * scale && my >= y && my <= y + getHeight() * scale;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = Math.max(0.5f, Math.min(3f, scale)); }
}

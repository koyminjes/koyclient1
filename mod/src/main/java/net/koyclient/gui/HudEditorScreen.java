package net.koyclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.koyclient.KoyClient;
import net.koyclient.config.KoyConfig;
import net.koyclient.module.KoyModule;

import java.util.List;

/** Drag HUD modules around; scroll over one to resize it. */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private KoyModule dragging;
    private double grabX, grabY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x55000000);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        for (KoyModule m : KoyClient.modules().all()) {
            if (!m.isEnabled() || !m.isHud()) continue;
            m.renderScaled(ctx);
            int x = m.getX(), y = m.getY();
            int w = Math.round(m.getWidth() * m.getScale());
            int h = Math.round(m.getHeight() * m.getScale());
            int color = (m == dragging || m.contains(mouseX, mouseY)) ? 0xFFFFFFFF : 0x88FFFFFF;
            ctx.drawBorder(x - 1, y - 1, w + 2, h + 2, color);
        }
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Drag to move - scroll to resize - Esc to save"), width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            List<KoyModule> mods = KoyClient.modules().all();
            for (int i = mods.size() - 1; i >= 0; i--) {
                KoyModule m = mods.get(i);
                if (m.isEnabled() && m.isHud() && m.contains(mx, my)) {
                    dragging = m;
                    grabX = mx - m.getX();
                    grabY = my - m.getY();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null && button == 0) {
            int maxX = Math.max(0, width - Math.round(dragging.getWidth() * dragging.getScale()));
            int maxY = Math.max(0, height - Math.round(dragging.getHeight() * dragging.getScale()));
            int nx = MathHelper.clamp((int) Math.round(mx - grabX), 0, maxX);
            int ny = MathHelper.clamp((int) Math.round(my - grabY), 0, maxY);
            dragging.setPosition(nx, ny);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        for (KoyModule m : KoyClient.modules().all()) {
            if (m.isEnabled() && m.isHud() && m.contains(mx, my)) {
                m.setScale(m.getScale() + (float) Math.signum(vAmount) * 0.1f);
                return true;
            }
        }
        return super.mouseScrolled(mx, my, hAmount, vAmount);
    }

    @Override
    public void close() {
        KoyConfig.save(KoyClient.modules());
        client.setScreen(parent);
    }
}

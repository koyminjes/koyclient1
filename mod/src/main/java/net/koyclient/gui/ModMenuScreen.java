package net.koyclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.koyclient.KoyClient;
import net.koyclient.config.KoyConfig;
import net.koyclient.module.KoyModule;

import java.util.List;

public class ModMenuScreen extends Screen {
    private final Screen parent;

    public ModMenuScreen(Screen parent) {
        super(Text.literal("KoyClient"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<KoyModule> mods = KoyClient.modules().all();
        int cols = 2, w = 150, h = 20, gap = 6;
        int startX = (width - (cols * w + (cols - 1) * gap)) / 2;
        int startY = 40;

        for (int i = 0; i < mods.size(); i++) {
            KoyModule m = mods.get(i);
            int cx = startX + (i % cols) * (w + gap);
            int cy = startY + (i / cols) * (h + gap);
            addDrawableChild(ButtonWidget.builder(label(m), btn -> {
                        m.setEnabled(!m.isEnabled());
                        btn.setMessage(label(m));
                    })
                    .dimensions(cx, cy, w, h)
                    .tooltip(Tooltip.of(Text.literal(m.getDescription())))
                    .build());
        }

        int rows = (mods.size() + cols - 1) / cols;
        int by = startY + rows * (h + gap) + 10;
        addDrawableChild(ButtonWidget.builder(Text.literal("Edit HUD Layout"),
                b -> client.setScreen(new HudEditorScreen(this)))
                .dimensions(width / 2 - 100, by, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 - 100, by + 26, 200, 20).build());
    }

    private Text label(KoyModule m) {
        return Text.literal(m.getName() + ": " + (m.isEnabled() ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        KoyConfig.save(KoyClient.modules());
        client.setScreen(parent);
    }
}

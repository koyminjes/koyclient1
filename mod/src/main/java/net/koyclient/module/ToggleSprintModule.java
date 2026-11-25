package net.koyclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/** Keeps you sprinting while you hold forward. */
public class ToggleSprintModule extends KoyModule {
    public ToggleSprintModule() {
        super("togglesprint", "Toggle Sprint", "Automatically sprints while moving forward.", true, 0, 0);
    }

    @Override
    public boolean isHud() {
        return false;
    }

    @Override
    public int getWidth() { return 0; }

    @Override
    public int getHeight() { return 0; }

    @Override
    protected void render(net.minecraft.client.gui.DrawContext ctx, net.minecraft.client.font.TextRenderer font) {}

    @Override
    public void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.currentScreen != null) return;
        if (mc.options.forwardKey.isPressed()
                && !p.isSneaking()
                && !p.isUsingItem()
                && !p.horizontalCollision
                && p.getHungerManager().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
    }
}

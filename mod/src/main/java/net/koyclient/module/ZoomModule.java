package net.koyclient.module;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Hold the zoom key to narrow the FOV. The FOV change itself lives in GameRendererMixin. */
public class ZoomModule extends KoyModule {
    private static final float MAX_ZOOM = 4f;

    private final KeyBinding key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.koyclient.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.koyclient"));

    private float factor = 1f;
    private boolean cinematicSetByUs;

    public ZoomModule() {
        super("zoom", "Zoom", "Hold the zoom key (default C) to zoom in.", true, 0, 0);
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
        boolean zooming = key.isPressed() && mc.currentScreen == null;
        float target = zooming ? MAX_ZOOM : 1f;
        factor += (target - factor) * 0.35f;

        // Cinematic camera while zoomed so aiming isn't twitchy.
        if (zooming && !mc.options.smoothCameraEnabled) {
            mc.options.smoothCameraEnabled = true;
            cinematicSetByUs = true;
        } else if (!zooming && cinematicSetByUs) {
            mc.options.smoothCameraEnabled = false;
            cinematicSetByUs = false;
        }
    }

    public float factor() {
        return factor;
    }
}

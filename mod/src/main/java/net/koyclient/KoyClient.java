package net.koyclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.koyclient.config.KoyConfig;
import net.koyclient.gui.HudEditorScreen;
import net.koyclient.gui.ModMenuScreen;
import net.koyclient.module.ModuleManager;
import org.lwjgl.glfw.GLFW;

public class KoyClient implements ClientModInitializer {
    public static final String MOD_ID = "koyclient";

    private static ModuleManager modules;
    private static KeyBinding menuKey;

    public static ModuleManager modules() {
        return modules;
    }

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        KoyConfig.load(modules);

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.koyclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.koyclient"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                client.setScreen(new ModMenuScreen(null));
            }
            modules.tick(client);
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden || mc.currentScreen instanceof HudEditorScreen) return;
            modules.renderHud(ctx);
        });
    }
}

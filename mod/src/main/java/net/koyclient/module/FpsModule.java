package net.koyclient.module;

import net.minecraft.client.MinecraftClient;

public class FpsModule extends TextModule {
    public FpsModule() {
        super("fps", "FPS", "Shows your frame rate.", true, 4, 4);
    }

    @Override
    protected String text() {
        return "FPS: " + MinecraftClient.getInstance().getCurrentFps();
    }
}

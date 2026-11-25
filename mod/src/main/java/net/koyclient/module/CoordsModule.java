package net.koyclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class CoordsModule extends TextModule {
    public CoordsModule() {
        super("coords", "Coordinates", "Shows your XYZ position.", true, 4, 36);
    }

    @Override
    protected String text() {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return "XYZ: -";
        return String.format("XYZ: %.0f / %.0f / %.0f", p.getX(), p.getY(), p.getZ());
    }
}

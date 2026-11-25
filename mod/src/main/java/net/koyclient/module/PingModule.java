package net.koyclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

public class PingModule extends TextModule {
    public PingModule() {
        super("ping", "Ping", "Shows your latency to the server.", true, 4, 52);
    }

    @Override
    protected String text() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return "Ping: -";
        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return e == null ? "Ping: -" : "Ping: " + e.getLatency() + " ms";
    }
}

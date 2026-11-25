package net.koyclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<KoyModule> modules = new ArrayList<>();

    public ModuleManager() {
        modules.add(new FpsModule());
        modules.add(new CpsModule());
        modules.add(new CoordsModule());
        modules.add(new PingModule());
        modules.add(new KeystrokesModule());
        modules.add(new ArmorModule());
        modules.add(new ToggleSprintModule());
        modules.add(new ZoomModule());
    }

    public List<KoyModule> all() {
        return modules;
    }

    public <T extends KoyModule> T get(Class<T> type) {
        for (KoyModule m : modules) {
            if (type.isInstance(m)) return type.cast(m);
        }
        return null;
    }

    public void tick(MinecraftClient mc) {
        for (KoyModule m : modules) {
            if (m.isEnabled()) m.tick(mc);
        }
    }

    public void renderHud(DrawContext ctx) {
        ClickTracker.poll();
        for (KoyModule m : modules) {
            if (m.isEnabled() && m.isHud()) m.renderScaled(ctx);
        }
    }
}

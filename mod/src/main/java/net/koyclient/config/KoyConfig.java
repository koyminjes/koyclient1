package net.koyclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.koyclient.module.ModuleManager;
import net.koyclient.module.KoyModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class KoyConfig {
    private static final Logger LOG = LoggerFactory.getLogger("koyclient");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static class Entry {
        boolean enabled;
        int x, y;
        float scale = 1f;
    }

    private KoyConfig() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("koyclient.json");
    }

    public static void load(ModuleManager mm) {
        Path p = path();
        if (!Files.exists(p)) return;
        try (Reader r = Files.newBufferedReader(p)) {
            Type type = new TypeToken<Map<String, Entry>>() {}.getType();
            Map<String, Entry> data = GSON.fromJson(r, type);
            if (data == null) return;
            for (KoyModule m : mm.all()) {
                Entry e = data.get(m.getId());
                if (e == null) continue;
                m.setEnabled(e.enabled);
                m.setPosition(e.x, e.y);
                if (e.scale > 0) m.setScale(e.scale);
            }
        } catch (Exception ex) {
            LOG.warn("Could not read koyclient.json, using defaults", ex);
        }
    }

    public static void save(ModuleManager mm) {
        Map<String, Entry> data = new LinkedHashMap<>();
        for (KoyModule m : mm.all()) {
            Entry e = new Entry();
            e.enabled = m.isEnabled();
            e.x = m.getX();
            e.y = m.getY();
            e.scale = m.getScale();
            data.put(m.getId(), e);
        }
        try (Writer w = Files.newBufferedWriter(path())) {
            GSON.toJson(data, w);
        } catch (Exception ex) {
            LOG.warn("Could not save koyclient.json", ex);
        }
    }
}

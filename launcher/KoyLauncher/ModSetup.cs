using System.Text.Json;

namespace KoyLauncher;

/// <summary>Puts the matching KoyClient mod build and Fabric API into the game's mods folder.</summary>
public static class ModSetup
{
    private static string ExeDir => Path.GetDirectoryName(Environment.ProcessPath) ?? AppContext.BaseDirectory;

    /// <summary>Finds koyclient-&lt;mcVersion&gt;-*.jar next to the launcher, or null if there is no build for that version.</summary>
    public static string? FindModJar(string mcVersion)
    {
        try
        {
            return Directory.GetFiles(ExeDir, $"koyclient-{mcVersion}-*.jar")
                .FirstOrDefault(f => !f.EndsWith("-sources.jar", StringComparison.OrdinalIgnoreCase));
        }
        catch
        {
            return null;
        }
    }

    public static async Task PrepareAsync(HttpClient http, string gameDir, string mcVersion, Action<string> log)
    {
        string mods = Path.Combine(gameDir, "mods");
        Directory.CreateDirectory(mods);

        // Remove jars this launcher manages so switching versions never leaves a wrong-version copy behind.
        foreach (var f in Directory.GetFiles(mods, "koyclient-*.jar")) File.Delete(f);
        foreach (var f in Directory.GetFiles(mods, "fabric-api-*.jar"))
        {
            if (!f.EndsWith($"+{mcVersion}.jar", StringComparison.OrdinalIgnoreCase)) File.Delete(f);
        }

        // 1) KoyClient mod build for this exact Minecraft version.
        string? jar = FindModJar(mcVersion);
        if (jar is null)
        {
            log($"No KoyClient build for Minecraft {mcVersion} yet, launching with Fabric only.");
        }
        else
        {
            File.Copy(jar, Path.Combine(mods, Path.GetFileName(jar)), overwrite: true);
            log("Installed " + Path.GetFileName(jar));
        }

        // 2) Fabric API from Modrinth (downloaded once per Minecraft version).
        if (Directory.GetFiles(mods, "fabric-api-*.jar").Length > 0) return;

        try
        {
            log("Downloading Fabric API...");
            string versionsParam = Uri.EscapeDataString("[\"" + mcVersion + "\"]");
            string loadersParam = Uri.EscapeDataString("[\"fabric\"]");
            string url = $"https://api.modrinth.com/v2/project/fabric-api/version?game_versions={versionsParam}&loaders={loadersParam}";

            using var doc = JsonDocument.Parse(await http.GetStringAsync(url));
            if (doc.RootElement.GetArrayLength() == 0)
            {
                log($"No Fabric API build found for Minecraft {mcVersion}, continuing without it.");
                return;
            }

            var files = doc.RootElement[0].GetProperty("files");
            JsonElement chosen = files[0];
            foreach (var f in files.EnumerateArray())
            {
                if (f.TryGetProperty("primary", out var p) && p.GetBoolean()) { chosen = f; break; }
            }

            string fileUrl = chosen.GetProperty("url").GetString()!;
            string fileName = chosen.GetProperty("filename").GetString()!;
            byte[] bytes = await http.GetByteArrayAsync(fileUrl);
            await File.WriteAllBytesAsync(Path.Combine(mods, fileName), bytes);
            log("Installed " + fileName);
        }
        catch (HttpRequestException ex)
        {
            log("Could not download Fabric API: " + ex.Message);
        }
    }
}

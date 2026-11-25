using System.Text.Json;

namespace KoyLauncher;

/// <summary>
/// Installs the Fabric loader by fetching the official version profile from Fabric's meta API
/// and dropping it into versions/. The launcher library then downloads Minecraft and the libraries.
/// </summary>
public static class FabricSetup
{
    public static async Task<string> EnsureProfileAsync(HttpClient http, string gameDir, string mcVersion, Action<string> log)
    {
        try
        {
            string listJson = await http.GetStringAsync($"https://meta.fabricmc.net/v2/versions/loader/{mcVersion}");
            string? loaderVersion = null;
            using (var doc = JsonDocument.Parse(listJson))
            {
                foreach (var entry in doc.RootElement.EnumerateArray())
                {
                    var loader = entry.GetProperty("loader");
                    if (loader.GetProperty("stable").GetBoolean())
                    {
                        loaderVersion = loader.GetProperty("version").GetString();
                        break;
                    }
                }
            }
            if (loaderVersion is null)
                throw new InvalidOperationException($"No stable Fabric loader found for Minecraft {mcVersion}.");

            string id = $"fabric-loader-{loaderVersion}-{mcVersion}";
            string dir = Path.Combine(gameDir, "versions", id);
            string file = Path.Combine(dir, id + ".json");
            if (!File.Exists(file))
            {
                string profile = await http.GetStringAsync(
                    $"https://meta.fabricmc.net/v2/versions/loader/{mcVersion}/{loaderVersion}/profile/json");
                Directory.CreateDirectory(dir);
                await File.WriteAllTextAsync(file, profile);
                log($"Installed Fabric loader {loaderVersion}");
            }
            return id;
        }
        catch (HttpRequestException)
        {
            // Offline: reuse a Fabric profile we already installed for this Minecraft version.
            string versions = Path.Combine(gameDir, "versions");
            if (Directory.Exists(versions))
            {
                var existing = Directory.GetDirectories(versions, $"fabric-loader-*-{mcVersion}")
                    .Select(Path.GetFileName)
                    .OrderByDescending(n => n)
                    .FirstOrDefault();
                if (existing is not null)
                {
                    log("Could not reach Fabric meta, using installed profile " + existing);
                    return existing;
                }
            }
            throw;
        }
    }
}

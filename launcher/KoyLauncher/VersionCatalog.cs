using System.Text.Json;

namespace KoyLauncher;

public record VersionInfo(string Id, string Type, bool FabricSupported);

/// <summary>Every Minecraft version Mojang lists, marked with whether Fabric supports it.</summary>
public static class VersionCatalog
{
    public static async Task<List<VersionInfo>> LoadAsync(HttpClient http)
    {
        string mojangJson = await http.GetStringAsync("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");

        var fabric = new HashSet<string>();
        try
        {
            string fabricJson = await http.GetStringAsync("https://meta.fabricmc.net/v2/versions/game");
            using var fd = JsonDocument.Parse(fabricJson);
            foreach (var e in fd.RootElement.EnumerateArray())
                fabric.Add(e.GetProperty("version").GetString()!);
        }
        catch (HttpRequestException)
        {
            // Fabric meta unreachable: versions just show up as Vanilla-only.
        }

        var list = new List<VersionInfo>();
        using var md = JsonDocument.Parse(mojangJson);
        foreach (var v in md.RootElement.GetProperty("versions").EnumerateArray())
        {
            string id = v.GetProperty("id").GetString()!;
            string type = v.GetProperty("type").GetString()!;
            list.Add(new VersionInfo(id, type, fabric.Contains(id)));
        }
        return list; // Mojang lists newest first
    }
}

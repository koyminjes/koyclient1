using System.Text.Json;

namespace KoyLauncher;

public class LauncherSettings
{
    public int MaxRamMb { get; set; } = 4096;
    public bool CloseOnLaunch { get; set; }

    public string SelectedVersion { get; set; } = "1.21.4";
    public bool UseFabric { get; set; } = true;
    public bool ShowSnapshots { get; set; }
    public bool ShowOld { get; set; }

    public static string GameDir =>
        Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".koyclient");

    private static string FilePath => Path.Combine(GameDir, "launcher.json");

    public static LauncherSettings Load()
    {
        try
        {
            if (File.Exists(FilePath))
                return JsonSerializer.Deserialize<LauncherSettings>(File.ReadAllText(FilePath)) ?? new();
        }
        catch
        {
            // Corrupt settings file: fall back to defaults.
        }
        return new();
    }

    public void Save()
    {
        Directory.CreateDirectory(GameDir);
        File.WriteAllText(FilePath, JsonSerializer.Serialize(this, new JsonSerializerOptions { WriteIndented = true }));
    }
}

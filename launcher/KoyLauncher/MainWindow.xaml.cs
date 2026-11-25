using System.Diagnostics;
using System.Windows;
using System.Windows.Input;
using CmlLib.Core;
using CmlLib.Core.Auth;
using CmlLib.Core.Auth.Microsoft;
using CmlLib.Core.ProcessBuilder;

namespace KoyLauncher;

public partial class MainWindow : Window
{
    private readonly HttpClient _http = new();
    private readonly LauncherSettings _settings = LauncherSettings.Load();
    private List<VersionInfo> _all = new();
    private MSession? _session;
    private bool _loading = true;

    public MainWindow()
    {
        InitializeComponent();
        _http.DefaultRequestHeaders.UserAgent.ParseAdd("KoyClient-Launcher/0.1");

        RamSlider.Value = _settings.MaxRamMb;
        CloseOnLaunchBox.IsChecked = _settings.CloseOnLaunch;
        SnapshotsBox.IsChecked = _settings.ShowSnapshots;
        OldBox.IsChecked = _settings.ShowOld;
        LoaderBox.SelectedIndex = _settings.UseFabric ? 0 : 1;
        GameDirText.Text = LauncherSettings.GameDir;
        UpdateRamLabel();
        _loading = false;

        Loaded += async (s, e) =>
        {
            await LoadVersionsAsync();
            await TrySilentSignInAsync();
        };
    }

    // ---------- Versions ----------

    private async Task LoadVersionsAsync()
    {
        SetStatus("Loading version list...");
        try
        {
            _all = await VersionCatalog.LoadAsync(_http);
            Log($"Loaded {_all.Count} Minecraft versions.");
        }
        catch (Exception ex)
        {
            Log("Could not load the version list: " + ex.Message);
            _all = new() { new VersionInfo(_settings.SelectedVersion, "release", true) };
        }
        RefreshVersionBox();
        SetStatus("Idle");
    }

    private void RefreshVersionBox()
    {
        bool wasLoading = _loading;
        _loading = true;

        var shown = _all
            .Where(v => v.Type == "release"
                     || (v.Type == "snapshot" && _settings.ShowSnapshots)
                     || ((v.Type == "old_beta" || v.Type == "old_alpha") && _settings.ShowOld))
            .Select(v => v.Id)
            .ToList();

        VersionBox.ItemsSource = shown;
        VersionBox.SelectedItem = shown.Contains(_settings.SelectedVersion)
            ? _settings.SelectedVersion
            : shown.FirstOrDefault();

        _loading = wasLoading;
        ApplyVersionSelection();
    }

    private void ApplyVersionSelection()
    {
        if (VersionBox.SelectedItem is not string id) return;
        _settings.SelectedVersion = id;

        bool fabricOk = _all.FirstOrDefault(v => v.Id == id)?.FabricSupported ?? false;

        bool wasLoading = _loading;
        _loading = true;
        LoaderBox.SelectedIndex = fabricOk && _settings.UseFabric ? 0 : 1;
        LoaderBox.IsEnabled = fabricOk;
        _loading = wasLoading;

        UpdateVersionText();
        if (!_loading) _settings.Save();
    }

    private bool UsesFabric()
    {
        bool fabricOk = _all.FirstOrDefault(v => v.Id == _settings.SelectedVersion)?.FabricSupported ?? false;
        return _settings.UseFabric && fabricOk;
    }

    private void UpdateVersionText()
    {
        string id = _settings.SelectedVersion;
        string loader;
        if (!UsesFabric()) loader = "Vanilla";
        else if (ModSetup.FindModJar(id) is not null) loader = "Fabric  |  KoyClient";
        else loader = "Fabric  |  no KoyClient build for this version yet";
        VersionText.Text = $"Minecraft {id}  |  {loader}";
    }

    private void VersionBox_Changed(object sender, System.Windows.Controls.SelectionChangedEventArgs e)
    {
        if (_loading) return;
        ApplyVersionSelection();
    }

    private void LoaderBox_Changed(object sender, System.Windows.Controls.SelectionChangedEventArgs e)
    {
        if (_loading) return;
        _settings.UseFabric = LoaderBox.SelectedIndex == 0;
        UpdateVersionText();
        _settings.Save();
    }

    private void VersionFilter_Click(object sender, RoutedEventArgs e)
    {
        if (_loading) return;
        _settings.ShowSnapshots = SnapshotsBox.IsChecked == true;
        _settings.ShowOld = OldBox.IsChecked == true;
        _settings.Save();
        RefreshVersionBox();
    }

    // ---------- Accounts (real Microsoft / Minecraft Java accounts) ----------

    private static async Task<MSession> SignInAsync(bool forcePrompt)
    {
        var handler = JELoginHandlerBuilder.BuildDefault();
        if (forcePrompt) return await handler.Authenticate();
        try { return await handler.AuthenticateSilently(); }
        catch { return await handler.Authenticate(); }
    }

    private async Task TrySilentSignInAsync()
    {
        try
        {
            var handler = JELoginHandlerBuilder.BuildDefault();
            _session = await handler.AuthenticateSilently();
            UpdateAccountUi();
        }
        catch
        {
            // No cached account yet: the user signs in with the button or on first Play.
        }
    }

    private void UpdateAccountUi()
    {
        AccountText.Text = _session?.Username ?? "Not signed in";
        SignInButton.Content = _session is null ? "Sign in with Microsoft" : "Switch account";
    }

    private async void SignIn_Click(object sender, RoutedEventArgs e)
    {
        SignInButton.IsEnabled = false;
        try
        {
            _session = await SignInAsync(forcePrompt: true);
            UpdateAccountUi();
        }
        catch (Exception ex)
        {
            Log("Sign-in failed: " + ex.Message);
        }
        finally
        {
            SignInButton.IsEnabled = true;
        }
    }

    // ---------- Play ----------

    private void SetPickersEnabled(bool on)
    {
        VersionBox.IsEnabled = on;
        SnapshotsBox.IsEnabled = on;
        OldBox.IsEnabled = on;
        LoaderBox.IsEnabled = on && (_all.FirstOrDefault(v => v.Id == _settings.SelectedVersion)?.FabricSupported ?? false);
    }

    private async void Play_Click(object sender, RoutedEventArgs e)
    {
        PlayButton.IsEnabled = false;
        SetPickersEnabled(false);
        Progress.Value = 0;
        try
        {
            if (VersionBox.SelectedItem is not string mc)
            {
                Log("No Minecraft version selected.");
                return;
            }

            if (_session is null)
            {
                SetStatus("Signing in...");
                _session = await SignInAsync(forcePrompt: false);
                UpdateAccountUi();
            }

            string gameDir = LauncherSettings.GameDir;
            Directory.CreateDirectory(gameDir);

            string versionName = mc;
            if (UsesFabric())
            {
                SetStatus("Setting up Fabric...");
                versionName = await FabricSetup.EnsureProfileAsync(_http, gameDir, mc, Log);

                SetStatus("Preparing mods...");
                await ModSetup.PrepareAsync(_http, gameDir, mc, Log);
            }

            var launcher = new MinecraftLauncher(new MinecraftPath(gameDir));
            launcher.FileProgressChanged += (s, args) => Dispatcher.Invoke(() =>
            {
                Progress.Maximum = Math.Max(1, args.TotalTasks);
                Progress.Value = args.ProgressedTasks;
                SetStatus($"Downloading game files ({args.ProgressedTasks}/{args.TotalTasks})");
            });

            var option = new MLaunchOption
            {
                Session = _session,
                MaximumRamMb = _settings.MaxRamMb
            };

            SetStatus("Checking game files...");
            Process process = await launcher.InstallAndBuildProcessAsync(versionName, option);

            SetStatus("Launching...");
            process.EnableRaisingEvents = true;
            process.Start();
            Log($"Started Minecraft {mc}.");

            if (_settings.CloseOnLaunch)
            {
                Close();
                return;
            }

            SetStatus("Game running");
            await process.WaitForExitAsync();
            Log($"Game exited with code {process.ExitCode}.");
            SetStatus("Idle");
        }
        catch (Exception ex)
        {
            SetStatus("Failed");
            Log("ERROR: " + ex.Message);
        }
        finally
        {
            Progress.Value = 0;
            PlayButton.IsEnabled = true;
            SetPickersEnabled(true);
        }
    }

    // ---------- Settings ----------

    private void RamSlider_Changed(object sender, RoutedPropertyChangedEventArgs<double> e)
    {
        if (_loading) return;
        _settings.MaxRamMb = (int)RamSlider.Value;
        UpdateRamLabel();
        _settings.Save();
    }

    private void Settings_Changed(object sender, RoutedEventArgs e)
    {
        if (_loading) return;
        _settings.CloseOnLaunch = CloseOnLaunchBox.IsChecked == true;
        _settings.Save();
    }

    private void UpdateRamLabel() => RamLabel.Text = $"{_settings.MaxRamMb / 1024.0:0.#} GB";

    private void OpenFolder_Click(object sender, RoutedEventArgs e)
    {
        Directory.CreateDirectory(LauncherSettings.GameDir);
        Process.Start(new ProcessStartInfo("explorer.exe", $"\"{LauncherSettings.GameDir}\"") { UseShellExecute = true });
    }

    // ---------- Navigation / window chrome ----------

    private void NavPlay_Click(object sender, RoutedEventArgs e) => ShowPanel(play: true);
    private void NavSettings_Click(object sender, RoutedEventArgs e) => ShowPanel(play: false);

    private void ShowPanel(bool play)
    {
        PlayPanel.Visibility = play ? Visibility.Visible : Visibility.Collapsed;
        SettingsPanel.Visibility = play ? Visibility.Collapsed : Visibility.Visible;
    }

    private void TitleBar_MouseDown(object sender, MouseButtonEventArgs e)
    {
        if (e.ChangedButton == MouseButton.Left) DragMove();
    }

    private void Minimize_Click(object sender, RoutedEventArgs e) => WindowState = WindowState.Minimized;
    private void Close_Click(object sender, RoutedEventArgs e) => Close();

    // ---------- Helpers ----------

    private void SetStatus(string text) => Dispatcher.Invoke(() => StatusText.Text = text);

    private void Log(string line) => Dispatcher.Invoke(() =>
    {
        LogBox.AppendText(line + Environment.NewLine);
        LogBox.ScrollToEnd();
    });
}

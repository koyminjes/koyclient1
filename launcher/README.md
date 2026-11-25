# KoyClient Launcher

WPF (.NET 8) launcher. Pick any Minecraft version Mojang lists (releases, optional snapshots and old alpha/beta),
choose Fabric + KoyClient or Vanilla, press Play. Requires a Minecraft: Java Edition account.

- Fabric is offered for every version Fabric supports; other versions run Vanilla.
- The KoyClient mod only loads when a jar named `koyclient-<minecraft version>-*.jar` sits next to the exe
  (the build workflow produces `koyclient-1.21.4-0.1.0.jar`). Other versions launch with Fabric + Fabric API only.
- Fabric API is downloaded from Modrinth per version. Settings: `%APPDATA%\.koyclient\launcher.json`.
- Sign-in uses the default handler of CmlLib.Core.Auth.Microsoft. Package versions float (`4.*`, `3.*`);
  if a CmlLib call fails to compile, adjust the few calls in `MainWindow.xaml.cs`.

# Get your KoyClient.exe (no coding tools needed)

GitHub can build the exe for you for free.

1. Unzip this folder.
2. Make a free account at https://github.com and click **New repository**. Name it `koyclient`, keep it Private, create it.
3. On the empty repository page click **uploading an existing file**. Open the unzipped folder and drag **everything inside it**
   (the `.github`, `mod` and `launcher` folders and the other files) into the browser. Click **Commit changes**.
4. Open the **Actions** tab. A build called "Build KoyClient" starts by itself (takes about 5-10 minutes).
   If it does not, click it, then **Run workflow**.
5. When it shows a green check, open that run and download **KoyClient** from the **Artifacts** section at the bottom.
6. Unzip it. `KoyClientLauncher.exe` and the `koyclient-1.21.4-...jar` must stay in the same folder. Double-click the exe.

If the build shows a red X: open it, copy the error text from the failed step, and send it to Claude to get it fixed.

Windows may show a SmartScreen warning for an unsigned exe. Click "More info" then "Run anyway".

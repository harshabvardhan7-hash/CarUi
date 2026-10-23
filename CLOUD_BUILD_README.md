# Build CarUI without a local laptop (cloud build)

This project includes `.github/workflows/android.yml`, which builds a debug APK in GitHub Actions.

## What is still required

The project must be uploaded to a GitHub repository and GitHub Actions must be enabled. I cannot sign in to your GitHub account or create/push a repository for you from this chat.

## Steps from an Android phone

1. In a mobile browser, create a new repository at https://github.com/new.
2. Upload the **contents** of this project to the repository's `main` branch. The `.github/workflows/android.yml` file must be included (some upload interfaces hide dot-folders; verify it is present).
3. Open the repository's **Actions** tab and select **Build CarUI APK**. If it did not start automatically, tap **Run workflow**.
4. Wait for the run to finish successfully. Open the run, scroll to **Artifacts**, and download `CarUI-debug-apk`.
5. Extract the downloaded artifact ZIP; it contains `app-debug.apk`. Open it on the Android device and allow installation from that source if Android asks.

The cloud build is not yet run, so the APK is not verified. The APK will be a debug build, not a Play Store release. Test it on the target head unit before relying on it.

# Distribution

How a build reaches a tester, and what has to be in place first.

## The two workflows

| Workflow | Fires on | Does |
| --- | --- | --- |
| [`ci.yml`](.github/workflows/ci.yml) | PRs to `main`, pushes to `main`, manual | Compiles, runs the full test suite, verifies the Roborazzi goldens, checks coverage |
| [`android-firebase-distribution.yml`](.github/workflows/android-firebase-distribution.yml) | `android-v*` tags, manual | Builds an installable APK and uploads it to Firebase App Distribution |

Both are thin triggers around Fastlane lanes, so the exact thing CI does is reproducible locally:

```bash
bundle exec fastlane android ci
```

## Cutting a tester build

```bash
git tag android-v1.0.1 && git push origin android-v1.0.1
```

That is the whole of it. The tag is the single source of truth for the version: `android-v1.0.1`
ships `versionName` `1.0.1` and `versionCode` `10000010`, derived by `release_build_number` in the
[Fastfile](fastlane/Fastfile). Nothing is hand-edited, and two releases cannot collide.

To build a tag that already exists, or to test the pipeline, use **Actions → Android → Firebase App
Distribution → Run workflow** and give it a `release_tag`. A run with no tag is refused rather than
shipped, because an untagged build calls itself `1.0.0-dev (1)` and would install over a real
release on a tester's device — tick `allow_untagged` if that throwaway is what you actually want.

`VERSION` is the fallback that dev builds are named after. Bump it when you start work on a new
release line; the tag, not this file, is what ships.

## Secrets

Set at **Settings → Secrets and variables → Actions** on the repository.

| Secret | Needed for | Where it comes from |
| --- | --- | --- |
| `GOOGLE_SERVICES_JSON` | Distribution | Firebase console → Project settings → Your apps → Android → download `google-services.json`, paste the **whole file** |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Distribution | Firebase console → Project settings → Service accounts → Generate new private key, paste the **whole file** |
| `ANDROID_KEYSTORE_BASE64` | Signed release builds (optional) | `base64 -i release.jks \| pbcopy` |
| `ANDROID_KEYSTORE_PASSWORD` | Signed release builds (optional) | The keystore password |
| `ANDROID_KEY_ALIAS` | Signed release builds (optional) | The key alias inside the keystore |
| `ANDROID_KEY_PASSWORD` | Signed release builds (optional) | The key password (equal to the store password on a PKCS12 keystore) |

The service account needs the **Firebase App Distribution Admin** role. Without it the build
succeeds and the upload fails with an authentication error that names none of this.

The Firebase app id to upload to is read out of `google-services.json` rather than hardcoded, so it
cannot disagree with the project the build was configured against — repoint `GOOGLE_SERVICES_JSON`
at a different Firebase project and the upload follows it, with a warning in the log. The lane fails
before building if that file has no Android app for `com.unomaster.pokedexgame`.

`google-services.json` is gitignored, so the distribution workflow writes it from the secret. It is
also the only build in the repo that runs with `firebase.enabled=true` — everything else compiles
against the logcat no-op in `src/androidNoFirebase` and needs no config at all.

## Debug builds, and switching to signed release builds

Without the four `ANDROID_*` secrets the distribution workflow builds the **debug** variant. That is
deliberate rather than a fallback that happens to work: an unsigned release APK cannot be installed,
so there is no useful "release build without a key". The debug APK is signed with the debug key,
installs, and runs.

What it costs: the build is debuggable and unminified, and a tester holding a debug build has to
uninstall it before they can take a release-signed build later — Android refuses the upgrade because
the signatures differ.

To switch, generate a keystore and add the four secrets:

```bash
keytool -genkeypair -v -keystore release.jks -alias pokedex -keyalg RSA -keysize 2048 -validity 10000
```

Keep `release.jks` somewhere safe and out of the repo — it is not recoverable, and losing it means
never being able to update the app under the same identity again. Then:

```bash
base64 -i release.jks | pbcopy
```

Nothing else changes. `Detect release signing secrets` starts finding the keystore, the
[`android-signing`](.github/actions/android-signing/action.yml) action stops being skipped, and the
lane builds `Release` instead of `Debug`. That action verifies all four credentials in about three
seconds, before the build — a wrong password otherwise surfaces six minutes in, as `Given final
block not properly padded`, which reads like a corrupt keystore rather than a wrong password.

For local release builds, copy `keystore.properties.PLACEHOLDER` to `keystore.properties`
(gitignored) and fill it in.

## Running the distribution lane locally

```bash
export FIREBASE_SERVICE_ACCOUNT_JSON="$HOME/secrets/pokedex-firebase.json"
export RELEASE_TAG="android-v1.0.1"
bundle exec fastlane android beta
```

You will also need the real `composeApp/google-services.json` in place. The lane checks both before
it starts building, so a missing or wrong credential costs seconds rather than a full build.

## iOS

`ci.yml` compiles the shared Kotlin/Native framework, which is the only automated check that reaches
the Kotlin/Native half of every `expect`/`actual`. It runs on merges to `main`, on demand, and on any
PR labelled `ios` — macOS runners bill at roughly ten times the Linux rate, so it deliberately does
not run on every push.

There is no TestFlight pipeline yet. Adding one means an App Store Connect API key, a `match` repo
for signing assets, and an `ios beta` lane.

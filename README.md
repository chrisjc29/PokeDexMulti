# PokeDex Game

Guess the Pokemon from its silhouette. A Compose Multiplatform app targeting **iOS and Android** from
one shared Kotlin codebase.

**Stack:** Compose Multiplatform · Koin (DI) · Ktor (networking) · Navigation 3 · Arrow (typed errors)
· Coil 3 (images) · Firebase Analytics + Crashlytics (native per platform, opt-in) · clean
architecture (data / domain / presentation) · MVI presentation.

## The feature, through every layer

Three screens — **Home** (start), **Game** (the round), **Settings**. One round flows through the
whole stack: the screen dispatches an `Intent` → `GameViewModel` calls `GetPokemonQuestionUseCase` →
which calls `PokemonRepository` → which calls a Ktor-backed `PokemonRemoteSource` twice (a page of
Pokemon, then the chosen one's detail) and logs one analytics event on the way. Coil loads the
artwork and a colour filter turns it into the silhouette.

Two things are injected that are usually read inline, both for the same reason — a test can't assert
on them otherwise:

- **`RandomSource`** decides which Pokemon is the answer and shuffles the choices.
- **`CurrentTimeProvider`** (plus the `TimeZone`) is the clock seam. Never call `Clock.System.now()`
  directly.

## Error handling

Anything that can fail for a reason the app understands returns `Either<DomainError, T>`. `DomainError`
(`domain/error/DomainError.kt`) is a sealed interface enumerating every failure the app models, and
`toUserMessage` is the single place that decides what each one says to the user — its `when` is
exhaustive, so adding a case forces you to word it.

Exceptions are reserved for bugs and for cancellation. The one place a `Throwable` becomes a value is
`network/NetworkCall.kt`, at the data boundary. Note the ordering there: Ktor 3's
`ContentConvertException` **is** an `IOException`, so it is matched first — otherwise an unparseable
response would tell the user to check their connection instead of being reported as the bug it is.

Compose failures with the Raise DSL rather than by branching:

```kotlin
suspend operator fun invoke(pageUrl: String?): Either<DomainError, PokemonQuestion> = either {
    val question = repository.getQuestion(pageUrl).bind()   // short-circuits on Left
    ensure(question.choices.size == 4) { DomainError.EmptyResponse }
    question
}
```

## Builds out of the box — Firebase is opt-in

This project **builds and runs with no Firebase setup**. Firebase is off by default
(`firebase.enabled=false` in `gradle.properties`): analytics and crash reporting go to logcat, and no
config files are required.

### Enabling Firebase (when you're ready)

1. Set `firebase.enabled=true` in `gradle.properties`.
2. **Android** — download `google-services.json` (Firebase console → Project settings → your Android
   app) and place it at `composeApp/google-services.json`, replacing the `.PLACEHOLDER`.
3. **iOS** — in Xcode: File → Add Package Dependencies → `https://github.com/firebase/firebase-ios-sdk`,
   add **FirebaseAnalytics** and **FirebaseCrashlytics** to the `iosApp` target, uncomment the
   `// ENABLE FIREBASE` blocks in `iosApp/iosApp/iOSApp.swift`, and add your `GoogleService-Info.plist`
   to the target.

With the flag on the build applies the google-services/Crashlytics plugins and compiles the real
SDK-backed analytics; with it off it compiles a logcat no-op. Common code is identical either way.

## Project layout

- `composeApp/` — the shared KMP module, and the Android application.
  - `commonMain` — `domain/`, `data/`, `presentation/` (MVI + `theme/` + `common/`), `navigation/`,
    `di/`, `network/`, `analytics/`.
  - `androidMain` — Android entry point, platform actuals.
  - `androidFirebase` / `androidNoFirebase` — the two analytics variants, selected by `firebase.enabled`.
  - `iosMain` — iOS entry point and Firebase bridge interfaces.
  - `commonTest` — domain, data and state-production tests, plus the shared `fake/` package.
  - `androidUnitTest` — `component/`, `feature/`, the Koin graph test, and the committed Roborazzi
    goldens in `screenshots/`.
- `iosApp/` — the SwiftUI host (`PokeDex.xcodeproj`).
- `fastlane/` — moved to the repo root when the old `androidApp` module was folded into `composeApp`.

## Run on Android

Open the project root in **Android Studio**, let Gradle sync, run the `composeApp` configuration.
Or:

```bash
./gradlew :composeApp:installDebug
```

## Run on iOS

1. Open `iosApp/PokeDex.xcodeproj` in **Xcode**.
2. The "Run Script" build phase already invokes
   `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`, and the framework search paths point at
   `composeApp/build/xcode-frameworks/…`. Without that phase, `import ComposeApp` won't resolve.
3. Select a simulator and run.

The shared framework is `baseName = "ComposeApp"`, `isStatic = true`. Renaming it means editing the
Xcode project too.

## Tests

```bash
./gradlew :composeApp:allTests            # domain, data, state production (all targets)
./gradlew :composeApp:testDebugUnitTest   # component + feature tiers + the Koin graph test
./gradlew recordRoborazziDebug            # write the screenshot baseline — run once, then commit
./gradlew verifyRoborazziDebug            # fail if the UI drifts from the goldens
./gradlew koverVerify                     # coverage gate on the logic layers
```

The regression tier auto-generates one screenshot test per `@Preview`. **To cover a new screen state,
add a `@Preview` for it** — no test code required. Composables are excluded from coverage on purpose,
so a screen without previews is a screen with no UI tests. Previews pass a `null` artwork URL so the
placeholder renders and no golden depends on the network.

Two test-infrastructure facts worth knowing before you add tests:

- **The UI tiers are debug-only.** `androidx.compose.ui:ui-test-manifest` supplies the
  `ComponentActivity` that `runComposeUiTest` launches, and it is a `debugImplementation` because it
  injects an activity into the merged manifest. The release unit-test variant is therefore disabled
  in `composeApp/build.gradle.kts`.
- **Transport tests run on a real dispatcher.** `runTest`'s virtual clock skips idle time instantly,
  which makes Ktor's `HttpTimeout` fire the moment a call suspends. See `networkTest` in
  `KtorPokemonRemoteSourceTest`.

## Back-stack restore

The back stack lives in a Koin-owned `Navigator`. It survives configuration changes but **not process
death** — Android may restore a backgrounded app at Home. That is accepted here: a three-screen game
with no deep stack loses nothing but the current round. `navigation/AppNavConfig.kt` is generated and
ready if you later want to persist it.

## Versions

All versions are pinned in `gradle/libs.versions.toml`. Bump there; keep Kotlin and Compose
Multiplatform aligned, since the Compose compiler ships with Kotlin. Two pins carry non-obvious
constraints and are commented in the catalog:

- **Arrow 2.2.2.1, not 2.2.3** — 2.2.3's Kotlin/Native klibs are ABI 2.4.0 and a Kotlin 2.3.0 compiler
  refuses to read them. Android compiles either way; only the iOS targets break.
- **Roborazzi and ComposablePreviewScanner are peers** — 1.59.0 + 0.9.2 is the pair verified here.
  Mismatch them and every generated preview test dies with the same `NoSuchMethodError`.

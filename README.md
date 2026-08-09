# Who's That

Guess the Pokemon from its silhouette. A Compose Multiplatform app targeting **iOS and Android** from
one shared Kotlin codebase — a single `composeApp` module holds the shared code *and* is the Android
application.

**Stack:** Compose Multiplatform 1.11 · Kotlin 2.3 · Koin (DI) · Ktor 3 (networking) · Navigation 3 ·
Arrow (typed errors) · Coil 3 (images) · Firebase Analytics + Crashlytics (native per platform,
opt-in) · clean architecture (data / domain / presentation) · MVI presentation.

## Screenshots

The whole UI is a handheld Pokédex — red shell, hinged screen bezel, scanline glass, LED cluster and
monospaced readouts. One round, start to finish:

<sub>**Home** and **Settings** are generated — halved straight from the Roborazzi goldens by
`./gradlew updateDocsScreenshots`, and CI fails if they drift from the UI. The three game shots are
hand-captured on a device on purpose: previews use an unresolvable artwork URL so no golden depends
on the network, so a generated one would draw the placeholder Pokéball exactly where these show the
silhouette and the revealed Pokémon. Re-capture those three by hand when the game UI changes.</sub>

<table>
  <tr>
    <td width="25%"><img src="docs/screenshots/home.png" alt="Home screen: the Pokédex closed, showing the title, the best streak and a START GAME button." width="100%"></td>
    <td width="25%"><img src="docs/screenshots/playing.png" alt="Game screen: a Pokémon silhouette on the scanline screen above four answer buttons." width="100%"></td>
    <td width="25%"><img src="docs/screenshots/wrong-guess.png" alt="Game screen after a wrong guess: the readout says NO MATCH, the choice is struck through and the red LED is lit." width="100%"></td>
    <td width="25%"><img src="docs/screenshots/solved.png" alt="Game screen solved: the artwork revealed in colour with the Pokémon's name, type and streak." width="100%"></td>
  </tr>
  <tr>
    <td align="center"><b>Home</b><br><sub>Start, and your best streak</sub></td>
    <td align="center"><b>Identify</b><br><sub>Silhouette + four choices</sub></td>
    <td align="center"><b>No match</b><br><sub>Wrong guesses burn a life</sub></td>
    <td align="center"><b>Match confirmed</b><br><sub>Artwork, type, streak</sub></td>
  </tr>
</table>

<!-- cmp-diagrams:start -->
## Architecture

Clean layers, one shared Kotlin module. **Almost every arrow points inward** — `domain` imports
nothing from `data`, `presentation` or any platform, and `data` touches `domain` only by implementing
its contracts or consuming them. Two things cut across all three layers: Koin, which constructs
everything, and `analytics/`, whose two interfaces the repository and every ViewModel hold. The one
edge that skips a layer is drawn below — the ViewModels reach `BestStreakStore` and `KeyValueStore`
directly, because one persisted integer and one boolean never earned a domain repository.

Colour means the same thing in every diagram here and in [docs/architecture.md](docs/architecture.md):
🔵 **presentation** · 🟢 **domain** · 🟠 **data** · ⚪ dashed grey **cross-cutting** (Koin, analytics,
platform, navigation plumbing) · 🔴 **failure path**. Every box is also grouped and labelled, so
nothing is carried by colour alone.

```mermaid
flowchart LR
    subgraph PRES["presentation"]
        SCREEN["HomeScreen · GameScreen · SettingsScreen<br/>stateless Content composables"]
        VMODEL["HomeViewModel · GameViewModel · SettingsViewModel<br/>State · Intent · Effect"]
    end
    subgraph DOM["domain"]
        USECASE["GetPokemonQuestionUseCase"]
        MODEL["PokemonQuestion"]
        DERROR["DomainError<br/>sealed interface"]
        REPOC["PokemonRepository"]
        RANDC["RandomSource"]
        CLOCKC["CurrentTimeProvider"]
    end
    subgraph DATA["data"]
        REPO["PokemonRepositoryImpl"]
        REMOTE["KtorPokemonRemoteSource"]
        DTO["PokemonPageDto · PokemonDetailDto"]
        RANDIMPL["KotlinRandomSource"]
        LOCAL["BestStreakStore · KeyValueStore"]
    end
    KOIN["Koin<br/>appModule · platformModule · navigationModule"]
    ANALYTICS["analytics/<br/>Analytics · CrashReporter"]

    SCREEN --> VMODEL
    VMODEL --> USECASE
    VMODEL --> CLOCKC
    VMODEL -->|"best streak · analytics opt-in<br/>no domain type"| LOCAL
    VMODEL -. "OperationViewModel reports" .-> ANALYTICS
    USECASE --> REPOC
    USECASE --> MODEL
    USECASE --> DERROR
    REPO -. implements .-> REPOC
    REPO --> RANDC
    REPO --> REMOTE
    REPO -. "logs pokemon_question_loaded" .-> ANALYTICS
    RANDIMPL -. implements .-> RANDC
    REMOTE --> DTO
    DTO -. maps to .-> MODEL
    KOIN -. constructs .-> VMODEL
    KOIN -. constructs .-> USECASE
    KOIN -. constructs .-> REPO

    classDef pres fill:#3987e522,stroke:#3987e5,stroke-width:2px
    classDef dom fill:#199e7022,stroke:#199e70,stroke-width:2px
    classDef dat fill:#d9592622,stroke:#d95926,stroke-width:2px
    classDef infra fill:#8b949e1f,stroke:#8b949e,stroke-width:1.5px,stroke-dasharray:4 3
    class SCREEN,VMODEL pres
    class USECASE,REPOC,RANDC,CLOCKC,MODEL,DERROR dom
    class REPO,REMOTE,RANDIMPL,LOCAL,DTO dat
    class KOIN,ANALYTICS infra
    style PRES fill:#3987e50d,stroke:#3987e5,stroke-width:1px
    style DOM fill:#199e700d,stroke:#199e70,stroke-width:1px
    style DATA fill:#d959260d,stroke:#d95926,stroke-width:1px
```

Two contracts are implemented outside `commonMain` and so have no implementer drawn above:
`CurrentTimeProvider` and `KeyValueStore` are `expect`/`actual` seams filled by `androidMain` and
`iosMain` — see the source-set diagram in [docs/architecture.md](docs/architecture.md).

### The MVI loop

A screen is a pure function of `State` plus an `onIntent` callback. All state lives in the ViewModel.
Navigation is not an `Effect` — it goes straight through `AppNavigator`, because navigating is what
the player asked for. `Effect` is reserved for genuinely one-shot things: the buzz on a wrong guess.

```mermaid
flowchart LR
    USER(["Player"])
    SCREEN["GameScreen"]
    VM["GameViewModel"]
    UC["GetPokemonQuestionUseCase"]
    NAV["AppNavigator"]

    USER -->|"taps a name"| SCREEN
    SCREEN -->|"onIntent(GameIntent)"| VM
    VM -->|"operation { either { } }"| UC
    UC -->|"Either&lt;DomainError, PokemonQuestion&gt;"| VM
    VM -->|"StateFlow&lt;GameState&gt;"| SCREEN
    VM -->|"GameEffect · Channel · fires once"| SCREEN
    VM -->|"navigate(route) · goBack()"| NAV
    SCREEN -->|"recomposes"| USER

    classDef pres fill:#3987e522,stroke:#3987e5,stroke-width:2px
    classDef dom fill:#199e7022,stroke:#199e70,stroke-width:2px
    classDef infra fill:#8b949e1f,stroke:#8b949e,stroke-width:1.5px,stroke-dasharray:4 3
    class SCREEN,VM pres
    class UC dom
    class NAV,USER infra
```

### One round, both outcomes

Expected failures are values, not exceptions. `network/NetworkCall.kt` is the only place a
`Throwable` becomes a `Left`, and `bind()` short-circuits everything above it — note that the second
request and the analytics event simply never run when the first one fails. The red band is the
failure path.

```mermaid
sequenceDiagram
    autonumber
    actor Player
    participant S as GameScreen
    participant VM as GameViewModel
    participant UC as GetPokemonQuestionUseCase
    participant REPO as PokemonRepositoryImpl
    participant NET as KtorPokemonRemoteSource
    participant API as PokeAPI

    Player->>S: tap PLAY AGAIN
    S->>VM: onIntent(PlayAgain)
    VM->>VM: state = GameState(isLoading = true)
    VM->>UC: invoke(nextPageUrl)
    UC->>REPO: getQuestion(pageUrl)
    REPO->>NET: fetchPage(pageUrl)
    NET->>API: GET a page of Pokemon
    alt page and detail both succeed
        rect rgba(25, 158, 112, 0.12)
            API-->>NET: 200 + JSON
            NET-->>REPO: Right(PokemonPageDto)
            REPO->>NET: fetchDetail(url of the random answer)
            NET->>API: GET that Pokemon
            API-->>NET: 200 + JSON
            NET-->>REPO: Right(PokemonDetailDto)
            REPO->>REPO: analytics.logEvent pokemon_question_loaded
            REPO-->>UC: Right(PokemonQuestion)
            UC-->>VM: Right, after ensure: four choices, one correct
            VM->>VM: state = artwork + choices, isLoading false
        end
    else transport error, 429, or a page too short to build a round
        rect rgba(227, 73, 72, 0.12)
            API-->>NET: failure
            NET-->>REPO: Left(DomainError)
            REPO->>REPO: or ensure(results >= 4) fails on a 200
            REPO-->>UC: Left, bind short-circuits
            UC-->>VM: Left(DomainError)
            VM->>VM: crashReporter, unless the server was just talking
            VM->>VM: state = errorMessage via toUserMessage
        end
    end
    VM-->>S: StateFlow emits
    S-->>Player: recomposition
```

More diagrams — source sets, navigation, the Koin graph, the error model and the test tiers — are in
[docs/architecture.md](docs/architecture.md).
<!-- cmp-diagrams:end -->

## The feature, through every layer

Three screens — **Home** (start), **Game** (the round), **Settings**. One round flows through the
whole stack: the screen dispatches an `Intent` → `GameViewModel` calls `GetPokemonQuestionUseCase` →
which calls `PokemonRepository` → which calls a Ktor-backed `PokemonRemoteSource` twice (a page of
Pokemon, then the chosen one's detail) and logs one analytics event (`pokemon_question_loaded`) on
the way. Coil loads the artwork and a colour filter turns it into the silhouette.

`https://pokeapi.co/api/v2/pokemon/` is the only literal URL in the app — every later page comes
from the API's own `next` cursor, which the `GameViewModel` keeps out of `GameState` because the
player never sees it. Nothing above the data layer knows PokeAPI exists.

**MVI, one folder per feature.** `GameScreen` / `GameState` / `GameIntent` / `GameEffect` /
`GameViewModel`. State re-emits; effects (`WrongAnswerFeedback`) go through a `Channel` so they fire
exactly once. `Home` has a `HomeState` of exactly one field — the best streak the player has ever
reached — but deliberately no `HomeEffect`: the start screen fires nothing one-shot, and an empty
effect type is a file to keep in step for no benefit. `Settings` is the same shape.

**`RandomSource`** is injected rather than called inline: it decides which Pokemon is the answer and
shuffles the choices, so a test can pin the round. **`CurrentTimeProvider`** (plus the `TimeZone`) is
the clock seam, wired through `platformModule` with a `FixedCurrentTimeProvider` fake in `commonTest`.
`GameViewModel` times each round through it — `roundStartedAt` when the artwork loads,
`solveSeconds` on the reveal — so the solve time is pinned in tests rather than raced against the
wall clock. Nothing reaches for `Clock.System.now()` directly.

### Settings

<table>
  <tr>
    <td width="30%"><img src="docs/screenshots/settings.png" alt="Settings screen: a single Share analytics toggle on the Pokédex screen." width="100%"></td>
    <td valign="top">

`SettingsViewModel` persists the analytics opt-in through the `KeyValueStore` expect/actual
(SharedPreferences on Android, `NSUserDefaults` on iOS). Note that today the preference is only
stored and re-read — the `Analytics` implementations do not consult it yet, so flipping the switch
does not currently mute event logging.

  </td>
  </tr>
</table>

## Error handling

Anything that can fail for a reason the app understands returns `Either<DomainError, T>`. `DomainError`
(`domain/error/DomainError.kt`) is a sealed interface enumerating every failure the app models, and
`toUserMessage` is the single place that decides what each one says to the user — its `when` is
exhaustive, so adding a case forces you to word it.

Exceptions are reserved for bugs and for cancellation. The one place a `Throwable` becomes a value is
`network/NetworkCall.kt`, at the data boundary. Note the ordering there: Ktor 3's
`ContentConvertException` **is** an `IOException`, so it is matched first — otherwise an unparseable
response would tell the user to check their connection instead of being reported as the bug it is.
This mapping only works because `expectSuccess = true` is set on the client; without it a 4xx is not
an exception at all and `body<T>()` fails with a confusing serialization error.

Compose failures with the Raise DSL rather than by branching:

```kotlin
suspend operator fun invoke(pageUrl: String?): Either<DomainError, PokemonQuestion> = either {
    val question = repository.getQuestion(pageUrl).bind()   // short-circuits on Left
    ensure(question.choices.size == CHOICE_COUNT) { DomainError.EmptyResponse }
    question
}
```

Two helpers carry the rest of the policy:

- **`OperationViewModel.operation()`** is the only way work leaves a ViewModel. It settles
  cancellation, crash reporting, and user-facing wording in one place — and it deliberately does
  *not* report `RateLimited` or `EmptyResponse`, because those are the server talking, not defects.
- **`bestEffort()`** (`domain/error/BestEffort.kt`) is for survivable failures such as a preference
  read: the caller continues with a fallback, but the failure is still recorded. It uses
  `Either.catch`, not `runCatching`, so `CancellationException` is rethrown rather than swallowed.

## Navigation

Navigation 3, with the back stack owned by a Koin `Navigator` rather than by composition. Routes are
a `@Serializable` sealed `AppRoute : NavKey`; `navigationModule` maps each one to its screen through
Koin's `navigation<Route> { }` DSL, so there is no growing `when` in the nav host.

Two things in `AppNavDisplay` are load-bearing and easy to delete by accident:

- **`entryDecorators` must include both** `rememberSaveableStateHolderNavEntryDecorator()` and
  `rememberViewModelStoreNavEntryDecorator()`. Without them a destination gets no `ViewModelStore`,
  `koinViewModel()` returns a fresh ViewModel on every recomposition, and the game refetches a
  Pokemon on every frame.
- **`Navigator.backStack` is typed `SnapshotStateList<Any>`, not `AppRoute`.** `koinEntryProvider()`
  builds an `(Any) -> NavEntry<Any>`; narrowing the list breaks type inference. The `AppNavigator`
  interface stays typed, so callers keep route safety.

The stack survives configuration changes but **not process death** — Android may restore a
backgrounded app at Home. That is accepted here: a three-screen game with no deep stack loses nothing
but the current round. `navigation/AppNavConfig.kt` already teaches serialization about every route
(reflection-based serialization does not exist on Kotlin/Native), so persisting the stack later is a
change in one place.

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
   at `iosApp/iosApp/`, replacing the `.PLACEHOLDER`.

With the flag on, `composeApp/build.gradle.kts` applies the google-services/Crashlytics plugins and
swaps the `androidNoFirebase` source set for `androidFirebase`; with it off it compiles a logcat
no-op. Common code only ever sees the `Analytics` / `CrashReporter` interfaces, so it is identical
either way. On iOS the Kotlin side talks to `AnalyticsBridge` / `CrashReporterBridge`, which Swift
implements — Kotlin never links the Firebase SDK.

Both real config files are git-ignored; only the `.PLACEHOLDER` copies are meant to be committed.

## Project layout

- `composeApp/` — the shared KMP module, and the Android application.
  - `commonMain` — `domain/`, `data/`, `presentation/` (MVI + `theme/` + `common/`), `navigation/`,
    `di/`, `network/`, `analytics/`.
  - `androidMain` — Android entry point (`MainApplication` starts Koin, `MainActivity` hosts
    `App()`), the manifest, and platform actuals.
  - `androidFirebase` / `androidNoFirebase` — the two analytics variants, selected by `firebase.enabled`.
  - `iosMain` — `MainViewController`, the Koin entry point, and the Firebase bridge interfaces.
  - `commonTest` — domain, data, navigation and state-production tests, plus the shared `fake/` package.
  - `androidUnitTest` — `component/`, `feature/`, the Koin graph test, `robolectric.properties`, and
    the committed Roborazzi goldens in `screenshots/`.
- `iosApp/` — the SwiftUI host (`PokeDex.xcodeproj`).
- `fastlane/` — lives at the repo root; it used to sit inside the deleted `androidApp` module.

## Run on Android

Open the project root in **Android Studio**, let Gradle sync, run the `composeApp` configuration.
Or:

```bash
./gradlew :composeApp:installDebug
```

`minSdk 24`, `targetSdk 35`, `compileSdk 36` — 36 is required because Compose 1.11 pulls
androidx.activity 1.12.x, which will not compile against anything lower.

## Run on iOS

1. Open `iosApp/PokeDex.xcodeproj` in **Xcode**.
2. The "Run Script" build phase already invokes
   `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`, and the framework search paths point at
   `composeApp/build/xcode-frameworks/…`. Without that phase, `import ComposeApp` won't resolve.
3. Select a simulator and run.

The shared framework is `baseName = "ComposeApp"`, `isStatic = true`, with an explicit
`bundleId` binary option. Renaming it means editing the Xcode project too.

Only **`iosArm64`** (devices) and **`iosSimulatorArm64`** (Apple-Silicon simulators) are built.
`iosX64` is deliberately absent: Compose Multiplatform dropped it in stable 1.11.0, so adding it back
fails to resolve. Intel Macs cannot run the simulator build.

## Tests

```bash
./gradlew :composeApp:allTests            # domain, data, navigation, state production (all targets)
./gradlew :composeApp:testDebugUnitTest   # component + feature tiers + the Koin graph test
./gradlew recordRoborazziDebug            # write the screenshot baseline — run once, then commit
./gradlew verifyRoborazziDebug            # fail if the UI drifts from the goldens
./gradlew updateDocsScreenshots           # re-cut the generated README images from those goldens
./gradlew koverVerify                     # 90% coverage gate on the logic layers
```

Goldens are committed to `composeApp/src/androidUnitTest/screenshots/` — deliberately in the source
tree, not the Gradle default under `build/`, which is git-ignored and would leave every CI run with
nothing to compare against.

The regression tier auto-generates one screenshot test per `@Preview`. **To cover a new screen state,
add a `@Preview` for it** — no test code required. Composables are excluded from coverage on purpose,
so a screen without previews is a screen with no UI tests. Previews pass a `null` artwork URL so the
placeholder Pokeball renders and no golden depends on the network.

Three test-infrastructure facts worth knowing before you add tests:

- **The UI tiers are debug-only.** `androidx.compose.ui:ui-test-manifest` supplies the
  `ComponentActivity` that `runComposeUiTest` launches, and it is a `debugImplementation` because it
  injects an activity into the merged manifest. The release unit-test variant is therefore disabled
  in `composeApp/build.gradle.kts`.
- **Transport tests run on a real dispatcher.** `runTest`'s virtual clock skips idle time instantly,
  which makes Ktor's `HttpTimeout` fire the moment a call suspends. See `networkTest` in
  `KtorPokemonRemoteSourceTest`.
- **`robolectric.properties` is not boilerplate.** It pins `application=TestApplication` (so the
  generated screenshot tests don't start Koin twice and die on the second preview), `sdk=34`,
  `graphicsMode=NATIVE` (Compose draws through Skia — the legacy mode yields empty canvases), and a
  `w480dp-h1600dp` display, because Robolectric's 320x470dp default pushes content below the fold.

## CI and release builds

Two GitHub workflows, both thin triggers around a Fastlane lane so the same checks run locally:

| Workflow | Fires on | Does |
| --- | --- | --- |
| `ci.yml` | PRs to `main`, pushes to `main` | Compiles, runs the suite, verifies the goldens and the generated README images, checks coverage |
| `android-firebase-distribution.yml` | `android-v*` tags, manual | Builds an APK and ships it to Firebase App Distribution |

```bash
bundle exec fastlane android ci           # exactly what CI runs
bundle exec fastlane android screenshots  # re-record the goldens + the generated README images
bundle exec fastlane android beta         # build, then upload to App Distribution ("testers")
bundle exec fastlane ios ci               # compile the Kotlin/Native framework
```

Push an `android-v1.0.1` tag and testers get that build. The tag is the single source of truth for
the version: Fastlane derives `versionName` `1.0.1` and `versionCode` `10000010` from it and passes
them to Gradle as `-PversionCode` / `-PversionName`, which default to `1` / `1.0` without them. An
untagged distribution run is refused rather than shipped.

Signing is read from `keystore.properties` (git-ignored — copy `keystore.properties.PLACEHOLDER`) or,
in CI, from `ANDROID_KEYSTORE_FILE` / `ANDROID_KEYSTORE_PASSWORD` / `ANDROID_KEY_ALIAS` /
`ANDROID_KEY_PASSWORD`. If neither is present the release build is left unsigned — so debug builds,
tests and CI checks keep working with no secrets, and the distribution lane builds the *debug*
variant instead, since an unsigned release APK cannot be installed.

See **[DISTRIBUTION.md](DISTRIBUTION.md)** for the secrets each workflow needs and how to switch to
signed release builds.

## Versions

All versions are pinned in `gradle/libs.versions.toml`. Bump there; keep Kotlin and Compose
Multiplatform aligned, since the Compose compiler ships with Kotlin. Three pins carry non-obvious
constraints and are commented in the catalog:

- **Arrow 2.2.2.1, not 2.2.3** — 2.2.3's Kotlin/Native klibs are ABI 2.4.0 and a Kotlin 2.3.0 compiler
  refuses to read them. Android compiles either way; only the iOS targets break.
- **Roborazzi and ComposablePreviewScanner are peers** — 1.59.0 + 0.9.2 is the pair verified here.
  Mismatch them and every generated preview test dies with the same `NoSuchMethodError`.
- **kotlinx-datetime 0.7.1, not 0.6.1** — a transitive dependency requires 0.7.x and Gradle takes the
  highest, so pinning lower only makes the catalog disagree with the compiler.

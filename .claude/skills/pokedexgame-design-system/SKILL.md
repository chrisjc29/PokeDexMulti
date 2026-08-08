---
name: pokedexgame-design-system
description: The PokedexGame design system — the handheld Pokédex device. Use whenever designing, adding or restyling any UI in this repo (a new screen, a new state on an existing screen, a shared composable), or when reviewing whether a proposed design fits. Covers the device token table, the component inventory and the rule for choosing between components, screen anatomy, copy voice, the clip/hit-testing trap, and the tests a new surface must land with.
---

# PokedexGame design system — the dex device

The app is a handheld Pokédex, not a Material app with red accents. Red moulded casing, an inset
darker case panel, a pale green LCD behind a dark bezel, raised black keys with a moulded lip, three
indicator lights and a big blue lens. Everything else follows from that.

**The code wins when this document and the source disagree.** If you change a token, change it in
`presentation/theme/` and update the table here in the same commit.

Source: `design_handoff_pokedex_device` (turn 3 of "Pokémon multiple choice game review").

---

## Tokens

Never write `Color(0xFF…)`, a bare `.dp`, or a `TextStyle` at a call site. Everything below has a
name; if what you need has no name, add one to the theme file rather than inlining it.

### Colours — `presentation/theme/AppColors.kt`, read via `LocalAppColors.current`

| Token | Hex | Use |
|---|---|---|
| `deviceShell` | `#B3141A` | Screen background — the outer casing |
| `deviceCase` | `#8B0F14` | The inset panel holding the LCD and keys |
| `deviceScreen` | `#DCE9D5` | LCD face |
| `deviceScreenInk` | `#2B2F3A` | Bezel, key face, primary text on the LCD |
| `deviceScreenInkDim` | `#4A5A44` | Body copy on the LCD |
| `deviceScreenInkFaint` | `#6B7A64` | Captions and meta on the LCD |
| `deviceScreenInkAlert` | `#B00020` | Alerts printed on the LCD (`NO MATCH`, `SIGNAL LOST`) |
| `deviceKeyEdge` | `#12151D` | The moulded lip under a raised key |
| `deviceKeyPressed` | `#191C24` | Face of a pressed-in (ruled-out) key |
| `deviceKeyLabel` | `#DCE9D5` | Key text |
| `deviceLens` / `deviceLensActive` / `deviceLensOffline` | `#7ECBFF` / `#9FDBFF` / `#4E7E9E` | Lens idle / solved / offline |
| `deviceScan` | `#5BC15B` | Scan bar, settings switch thumb |
| `ledRedIdle` `ledRed` `ledRedOn` | `#E23B3B` `#7A2222` `#FF4D4D` | Red light: idle / dark / lit |
| `ledAmberIdle` `ledAmber` `ledAmberOn` | `#F5C542` `#8A6B22` `#F5C542` | Amber light, and the live streak counter |
| `ledGreenIdle` `ledGreen` `ledGreenOn` | `#5BC15B` `#2F5F2F` `#4ADE80` | Green light; `ledGreen` also prints `MATCH CONFIRMED` |
| `wrongLabel` | `#FF6B6B` | Struck-through name on a ruled-out key |
| `revealFill` | `#F5C542` | The glow behind revealed artwork |

Existing brand marks are unchanged: `pokeballRed` `#EE1515`, `pokeballShell` `#FFFFFF`,
`pokeballOutline` `#1B1B1B`, `pokeballCore` `#C1D4E3`, `silhouette` `#2B2F3A`.

Device values are identical in `LightAppColors` and `DarkAppColors`, because the casing is a physical
object and does not repaint itself at night. Text drawn on the casing itself is white at 85% (chrome
meta), 75% (section labels) or 50% (a muted counter) — those alphas live as private constants beside
the composables that use them.

`MaterialTheme.colorScheme` still dresses the components Material owns (snackbar, ripple). Anything
sitting on the LCD takes a device token instead, so it stays legible on pale green in either theme.

### Type — `presentation/theme/Typography.kt`

Device chrome is **monospace with letter-spacing** (`DexTypography`); prose is the system font
(`MaterialTheme.typography`). A paragraph set in monospace reads measurably slower, and the dex entry
is the only real prose the app has.

| `DexTypography.*` | Size / weight / tracking | Use |
|---|---|---|
| `Title` | 20/26 SemiBold, 1sp | `POKÉDEX GAME` on the home LCD |
| `Heading` | 22 SemiBold, 1sp | `PIKACHU` in the dex entry |
| `ChromeTitle` | 13 SemiBold, 1.5sp | `SETTINGS` on the casing, `SIGNAL LOST` |
| `HeaderMeta` | 11 SemiBold, 1sp | `ROUND 04`, `NO. 025`, `SETTINGS` on home |
| `SectionLabel` | 11 SemiBold, 1.5sp | `IDENTIFY THE SPECIMEN`, `BEST STREAK · 12` |
| `Counter` | 11 SemiBold, 1sp | `STREAK 3`, the `ELECTRIC` type tag |
| `StatusLine` | 10 SemiBold, 1sp | Printed inside the LCD |
| `LoadingLabel` | 12 SemiBold, 1.5sp | `LOADING SPECIMEN…` |
| `KeyLabel` | 14 SemiBold | A name key |
| `PrimaryKeyLabel` | 15 SemiBold, 1.5sp | The one big action key |
| `SecondaryKeyLabel` | 13 SemiBold | `END` |
| `OutlinedKeyLabel` | 12 SemiBold, 1sp | `DISMISS` |
| `EntryBody` | 13/18, system font | The dex entry's description line |

### Shape, size, motion

`DeviceShapes` (`AppShapes.kt`): `Case` = 12/12/**26**/12 — the asymmetric bottom-end corner is what
makes the panel read as moulded plastic rather than a card; `CaseCompact` = 24 on that corner;
`Screen` = 8dp; `Key` = 10dp; `PrimaryKey` = 12dp.

`Dimens` carries every device measurement (`Device*`, `GameScreenHeight`, `SilhouetteSize`, …). A few
sit off the 4dp grid on purpose — 9dp key gaps, 13dp lights, 3dp bezels — because they describe
hardware, not layout.

`Motion`: `DurationScanSweep` 2600 (the sweep, `FastOutSlowInEasing`), `DurationSpin` 1400 (linear),
`DurationShake` 350, `DurationReveal` 300. The first two are ambient — the machine saying it is
working — which is why they are far longer than the two that react to a guess.

---

## Components — `presentation/common/`

| Composable | Use it for | Instead of |
|---|---|---|
| `DeviceScaffold` | Every screen, without exception | `Scaffold`, `TopAppBar` |
| `DeviceScreenPanel` | Anything on the LCD: the stage, the dex entry, the settings row | `Card`, `Surface` |
| `DevicePrimaryKey` | The **one** main action on a screen | `Button` |
| `DeviceKey` | A key in a grid of equals — the four names | `OutlinedButton` |
| `DeviceOutlinedKey` | The quieter key beside or below a primary one (`END`, `DISMISS`) | `TextButton` |
| `DeviceLens` / `DeviceLeds` | Chrome only — `DeviceScaffold` places them | — |
| `Pokeball` | The drawn brand mark, any size | an image asset |
| `PokemonArtwork` | The specimen, silhouetted or revealed | `AsyncImage` |

Choosing between keys: **one `DevicePrimaryKey` per screen, never two.** If a screen seems to need
two equally-weighted actions, one of them is a `DeviceOutlinedKey` — see the solved screen, where
`NEXT SPECIMEN` is raised and `END` is cut into the casing.

`DeviceKey(pressed = true)` sinks a key instead of recolouring it, and the lip takes the face colour
so the key keeps its exact height in the grid. Pair it with `enabled = false`: a ruled-out key stays
legible because the player earned that information, but it is dead.

### The clip trap — read this before adding a rounded container

**Draw rounded shapes with `background(colour, shape)`. Do not use `Modifier.clip(shape)` on any
container that has children inside its padding.** Under `clip`, pointer input stops reaching those
children: the UI renders perfectly and every key inside silently does nothing. It is invisible to a
screenshot test and it is exactly what the component tests are there to catch.

Where a decoration genuinely has to stop at a rounded edge — scanlines, the scan bar, the reveal
glow, the case's inner shadow — put it in its own `Box(Modifier.matchParentSize().clip(shape))`
overlay that contains nothing tappable. `DeviceScreenPanel` and `DeviceCase` both do this already, so
a new screen gets it for free.

---

## Screen anatomy

Every screen is `DeviceScaffold { … }`, outside in:

1. **Chrome row** — lens, optional stamped `title`, optional `lights`, optional right-aligned
   `metaLabel`. The lens is the back control whenever `onNavigateBack` is passed; the design has no
   back arrow and iOS has no system gesture, so the biggest circle on the casing is the way out. A
   tappable `metaLabel` (Home's `SETTINGS`) carries the 48dp minimum via `sizeIn`.
2. **Case** — fills the rest, `caseGap` between children (`DeviceCaseGapLarge` on Home,
   `DeviceCaseGap` normally, `DeviceCaseGapCompact` on the error screen).
3. **Content** — LCD panels and keys.

`compact = true` runs the smaller casing used by loading and error: 38dp lens, 11dp lights, tighter
margins, 24dp bottom-end corner.

Lights say what the dex is doing, and only that: `Idle` while nothing has happened, `Alert` on a
wrong guess or a failed load, `Confirmed` on a solve. A screen with nothing to report passes
`lights = null` — three lit lamps over a preferences panel would be saying something untrue.

Long content sits in a `Column(Modifier.weight(1f).verticalScroll(…))` inside the case, so a large
font scale or a short device scrolls rather than clipping the keys off the bottom. Anything pinned to
the bottom of the casing (the solved screen's key row) goes outside that scrolling column.

**State stays theme-agnostic.** `GameState` holds booleans, sets and numbers; which light comes on,
which key sinks and which colour a label takes is decided at render time from `LocalAppColors`. No
ViewModel ever names a colour.

---

## Copy voice

- Device chrome is **upper case, terse, machine-like**: `SCANNING…`, `NO MATCH · 2 LEFT`,
  `MATCH CONFIRMED`, `IDENTIFY THE SPECIMEN`, `NEXT SPECIMEN`.
- Prose to the player is **sentence case, plain, no exclamation marks**: "Guess the Pokémon from its
  silhouette.", "Identified in 4 seconds. Streak now 4 — a wrong guess resets it."
- Errors say what happened *and* what to do: `SIGNAL LOST` over "Couldn't connect. Check your
  connection and try again.", with `RETRY` and `DISMISS` under it.
- **Never print a number the app does not have.** No `BEST STREAK · 0` before a streak exists, no
  type tag when the API sent none, no "Identified in 0 seconds". Drop the line instead.

---

## What a new surface lands with

1. One `@Preview` per state it can be in — loading, empty, error, loaded, and any long-text case.
   The previews are the source of the Roborazzi goldens, so a state without one has no visual
   contract. Record with `./gradlew recordRoborazziDebug`, and commit the goldens.
2. A **component test** in `androidUnitTest/component/` asserting on what the player sees and on the
   intent each tap emits. This tier is what catches the clip trap above — a golden will not.
3. A **feature test** in `androidUnitTest/feature/` if the surface has a ViewModel loop, driving the
   real ViewModel over fakes.
4. **Unit tests** in `commonTest` for any new state production, asserting on `Either` results.

`./gradlew testDebugUnitTest verifyRoborazziDebug koverVerify` is the gate.

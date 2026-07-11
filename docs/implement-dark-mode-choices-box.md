# Dark mode for ChoicesBox — implementation plan

Goal: port the legacy light/dark look into the current Compose `ChoicesBox`, cleaned up.
Default = **dark**. The user switches to light via a button in the box header.

Package: `com.purpletear.game.presentation.game_play.components.choices_box`
(current file: `ChoicesBox.kt`; only caller: `SmsGameScreen` → private `AnimatedChoicesBox`).
Legacy reference (get inspired, do not copy): `/Users/hb/Documents/Coding/Android/backup/smsgame - 2026:02:27/src/main/`
(`res/layout/inc_choice_box.xml`, `.../smsgame/SmsGameGraphics.kt#setDarkModeTheme`,
`com.example.sharedelements.DarkModeHelper`).

## 1. Legacy look to reproduce (literal values)

| Element            | Dark                      | Light                     |
|--------------------|---------------------------|---------------------------|
| Box background     | `#1e1e1e`                 | `#FEFEFE`                 |
| Title text         | `#e2e2e2`                 | `#007eff`                 |
| Choice text        | `#e2e2e2`                 | `#7C000000` (`#2C2C2C`)   |
| Divider            | `#222222`                 | `#e2e2e2`                 |
| Scrim              | `#AA000000` (both modes)  |                           |
| Corner radius      | 16dp                      | 16dp                      |

Toggle: 60dp header button, 16dp icon, swapping `ic_darkmode` (shown in light) /
`ic_darkmode_white` (shown in dark). No "selected choice" visual state exists in either codebase.

## 2. Design (keep it small and explicit)

- Add two params to `ChoicesBox`: `isDarkMode: Boolean = true` and `onToggleDarkMode: () -> Unit = {}`.
  Defaults keep the existing preview/callers working without changes.
- Derive colors locally with a tiny private `ChoicesBoxColors(isDarkMode)` (data class of the 5
  values above). **Do not** rewire the app theme: the box currently mixes Material3 widgets inside a
  Material2 `SutokoTheme` with hardcoded colors, so a local derived palette is the minimal, safe
  choice and avoids scope creep.
- Header becomes a `Row`: title (start, weight 1) + `IconButton` (end). **Team decision (Romain):**
  one vector asset tinted by state (`tint = if (isDarkMode) … else …`) — no dual-PNG swap, less memory
  and overdraw. Only fall back to two assets if design strictly requires the legacy dual-icon look.
- Update `ChoiceRow` text color from the same palette (currently hardcoded default).
- Keep scrim, shape (use 16dp to match legacy; currently 12dp — pick one and apply consistently),
  widths and haptics as-is.

## 3. State & persistence — team decision

State owner: `GameEngineViewModel` (`GameUiState`). Thread the value down the existing path:
`GameUiState` → `GameScreenNavigation` → `SmsGameScreen` → `AnimatedChoicesBox` → `ChoicesBox`.

**Approach: persist, with a dedicated key, default = dark.**

- Add `isChoicesDarkMode: Boolean = true` to `GameUiState` and a VM method `onToggleChoicesDarkMode()`.
- Hydrate once at VM init from a dedicated `SharedPreferences` key `SUTOKO_CHOICES_DARK_MODE`
  (default `true`); write it on each toggle. No reactive `Flow` needed — only this screen consumes it
  (Gerard: bounded, obvious control flow).
- Do **not** reuse/flip the existing `DarkModeHelper` (it defaults to light and is shared). A dedicated
  key preserves its contract and documents our dark default explicitly (Liskov).
- Keep `ChoicesBox` presentational and backward compatible: `isDarkMode: Boolean = true` and
  `onToggleDarkMode: () -> Unit = {}` default so previews/legacy callers keep compiling (Liskov/Leland).
- Wrap the palette in `remember(isDarkMode)`; keep `ChoiceRow` allocation-free in the loop (Romain).
- Add `@Preview` for both dark and light (Leland).

## 4. Assets & strings

- Port/verify one toggle icon (`ic_darkmode` as a vector) under `game/presentation/.../res/drawable`.
- Persist the boolean via the existing app's `SharedPreferences` (key `SUTOKO_CHOICES_DARK_MODE`,
  default `true`). Reuse the project's standard prefs access if one exists; otherwise a single
  dedicated read/write — no new abstraction layer.
- Add a `contentDescription` string for the toggle (a11y); min touch target ≥ 48dp.

## 5. Out of scope / notes

- No selected-choice highlight (neither codebase has it).
- No Material theme migration; no `values-night`; this is an in-app toggle only.
- Keep `MakeAChoiceButton` unchanged unless design wants it themed too.

## 6. Acceptance checklist

- Opens in **dark** by default; toggle switches all 5 colors at once with no flicker.
- Light mode matches the legacy `#FEFEFE` / `#007eff` / dark text look.
- Close-on-scrim-tap, choice tap + haptic, and `AnimatedChoicesBox` fade still work.
- Toggled theme survives process death; other features' dark mode is unaffected.

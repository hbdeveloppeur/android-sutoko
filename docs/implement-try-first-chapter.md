# Try the 1st chapter — implementation plan

Status: **Plan — awaiting approval.** No code has been written yet.

## 1. Goal

For **non‑free (paid) SMS games**, let a user play **only the first chapter** without buying,
then ask them to purchase to continue.

Three observable states:

| # | Context | GamePreview buttons | In‑game chapter‑end message |
|---|---------|---------------------|------------------------------|
| A | Paid, not owned, not started (chapter 1) | **Buy** + **Try the 1st chapter** | normal "next chapter" (n/a — trial not started) |
| B | Trial in progress (playing chapter 1) | — | at chapter 1 end: **"Trial finished" → Buy** (replaces `MessageNextChapter`) |
| C | Trial done, still not owned (progress advanced to chapter 2) | **Buy** + **Restart** | — |

Free games, owned games, and the purchase/confirm flow are **unchanged**.

## 2. Current behavior (verified in code)

- Button state machine is two pure mappers (no VM/composable logic):
  `GameItem.toGameActionState(...)` (`model/GameActionState.kt:24‑44`, first‑match `when`) then
  `GameActionState.toButtonsState(onAction)` (`common/states/GameButtonsState.kt:46‑149`).
- The paid‑not‑owned gate is `GameActionState.kt:35` `!isFree && !isPurchased -> Purchase`.
  `Purchase` is a `data object` (no chapter info). Its rendering (`GameButtonsState.kt:132‑140`)
  hardcodes `restartLeftButton(1, …)`, so the left button is **always hidden** → today a
  paid‑not‑owned game shows **only "Buy"** at any chapter.
- `isFree = catalog.price == 0` (`model/GameItem.kt:49`); `GameCatalog.isPremium() = price > 0`.
- Ownership is computed in `GamePreviewViewModel.kt:77` as `catalog.skus.any { it in purchasedSkus }`.
  Global premium (`observeHasGlobalPremium()`, `:100‑105`) currently drives **only** the
  "Premium Active" label — it is **not** treated as ownership (latent inconsistency;
  `PurchaseRepository.observeIsPurchased(skus)` *does* include global premium).
- "First chapter" = `Chapter.number == 1` / code `"1A"` (default current chapter, hardcoded in
  `UserGameProgress`, `ChapterRepositoryImpl.DEFAULT_CHAPTER_CODE`).
- Entry to play: `GamePreviewViewModel.navigateToPlay()` (`:146‑164`) →
  `GamePreviewEvent.PlayGame(gameId, legacyId, isPurchased)` → `MainActivity.kt:257‑269`
  `onNavigateToGame` → `startSmsGameActivity(gameId, chapterCode = currentChapter.normalizedCode)`
  (`:519‑532`). The played chapter is the user's progress chapter (1A when not started).
- Chapter end (engine, purchase‑agnostic): `ChapterChangeNodeHandler` emits
  `HandlerEffect.ChangeChapter(nextCode)`; `GameEngine.applyEffect` (`GameEngine.kt:449‑458`)
  does `memory.setCurrentChapter(nextCode)` + `memory.save()` (progress **advances to the next
  chapter**) and appends a `GameMessageNextChapter()` (type `ChapterEnd`); the resolver then sets
  `GameEngineState.ChapterFinished` (`:503‑506`). The CTA exists because a `ChapterEnd` message is
  present, rendered by `MessageItemMapper.kt:68‑76` → `MessageNextChapter`, with
  `showButton = state.showNextChapterButton` and click → `GameEngineViewModel.onNextChapterClicked()`
  (`:582‑584`) → `_navigateToNextChapter` channel → `SmsGameActivity` navigates to the next graph.

Pinned (must keep green): engine `ChangeChapter` + `ChapterFinished` + appended
`GameMessageNextChapter` (`GameEngineTest`); `startFromNode` clears messages; `resolveStartNodeId`
policy; preview VM behaviors; `GamePreviewPurchaseHandler` flags. `toGameActionState` /
`toButtonsState` / `navigateToPlay` / `onNextChapterClicked` have **no** tests today.

## 3. Design decisions (team consensus)

- **Engine stays purchase‑agnostic (Holzmann/Liskov).** No trial/ownership logic in `game/domain`.
  Gating is presentation‑only. This preserves every pinned engine test.
- **Detect "trial done" from existing progress, not a new store (Holzmann).** Because the engine
  already persists the next chapter at the boundary, a finished trial simply means
  `currentChapter.number > 1` while still not owned → state **C** falls out of the same gate.
  (Verified at `GameEngine.kt:451‑452`; re‑confirm the save target in step 1.)
- **One state, contextual left button (Uncle Bob).** Turn `Purchase` into
  `data class Purchase(chapterNumber: Int, showTry: Boolean)`. The right button is always **Buy**;
  the left button is **Try** (chapter 1, trial‑eligible) or **Restart** (chapter > 1) or hidden
  (legacy). This matches A and C with a single branch and fixes the hardcoded `1`.
- **Explicit trial flag across the Activity boundary (Liskov: validate at boundaries).** A new
  `isTrial: Boolean` is threaded `GamePreview → MainActivity → SmsGameActivityArgs →
  SavedStateHandle → GameEngineViewModel → GameUiState`. The play screen swaps the chapter‑end UI
  purely from `state.isTrial`. No engine change, no stale derived state.
- **Reuse `MessageNextChapter` for the trial‑finished message (Romain/Holzmann).** Same composable,
  different title/button copy and click target — no new component, no extra layout work.
- **Ownership = owned‑SKU OR global premium (Liskov: honor the purchase contract).** Fold global
  premium into the preview's `isPurchased` so a premium subscriber is treated as having full
  access (consistent with `PurchaseRepository.observeIsPurchased`). See §6.

## 4. Changes by file

### 4.1 Ownership consistency — `game_preview/GamePreviewViewModel.kt`
In the `combine` (`:66‑95`), also include `observeHasGlobalPremium()` and set
`isPurchased = catalog.skus.any { it in purchasedSkus } || hasGlobalPremium`. Keep
`isUserPremium` for the label. (Net: `isPurchased` now means "has full access".)

### 4.2 Entry gate — `model/GameActionState.kt`, `model/GameItem.kt`
- `GameItem`: carry `val legacyId: Int?` (= `catalog.legacyId` in the secondary ctor;
  default `null` in the primary ctor). Do not collapse it to a Boolean — we need the
  actual id to tell Friendzoned apart from other legacy games.
- `Purchase`: `data object` → `data class Purchase(val chapterNumber: Int, val showTry: Boolean)`.
- `toGameActionState` Purchase branch:
  `showTry = legacyId !in FRIENDZONED_LEGACY_IDS && (currentChapter?.number ?: 1) <= 1`,
  where `FRIENDZONED_LEGACY_IDS = setOf(159, 160, 161, 162, 163)` (kept in sync with
  app `FriendzonedGameRouter`). `showTry` is false **only** for Friendzoned games; a
  non-null `legacyId` alone is not enough to suppress Try (see §7).

### 4.3 Entry buttons — `common/states/GameButtonsState.kt`, `GamePreviewAction.kt`
- `GamePreviewAction`: add `data object OnTry`.
- `toButtonsState` `Purchase` branch (`:132‑140`):
  - `left = if (showTry) tryFirstChapterLeftButton(onAction) else restartLeftButton(chapterNumber, onAction)`
  - `right` = Buy / `OnBuy` (unchanged).
- Add `tryFirstChapterLeftButton(onAction)` → title `game_menu_try_first_chapter`,
  subtitle `game_menu_try_first_chapter_subtitle`, `onClick = OnTry` (same look as `restartLeftButton`).

### 4.4 Entry action — `game_preview/GamePreviewViewModel.kt`, `events/GamePreviewEvent.kt`
- `GamePreviewEvent.PlayGame`: add `val chapterCode: String? = null`, `val isTrial: Boolean = false`.
- `onAction`: `GamePreviewAction.OnTry -> navigateToPlay(isTrial = true)`.
- `navigateToPlay(requestNickName: Boolean, isTrial: Boolean = false)`: same nickname gate
  (`userNickNameRequired && currentChapter.number == 1`); emit
  `PlayGame(gameId, legacyId, isPurchased, chapterCode = currentChapter.value?.normalizedCode, isTrial)`.
  `OnPlay` calls it with `isTrial = false` (behavior unchanged).

### 4.5 Activity wiring — `app/.../MainActivity.kt`
- `onNavigateToGame(gameId, legacyId, isGranted, chapterCode, isTrial)` (`:257`): for friendzoned,
  ignore `isTrial` (launch as today). For SMS: `startSmsGameActivity(gameId, chapterCode = chapterCode ?: viewModel.currentChapter.value?.normalizedCode, isTrial = isTrial)`.
- `startSmsGameActivity(..., isTrial: Boolean = false)` → `SmsGameActivityArgs(..., isTrial = isTrial)`.

### 4.6 Play args/route — `game_play/SmsGameActivityArgs.kt`, `SmsGameRoutes.kt`, `navigation/GameScreenNavigation.kt`, `SmsGameActivity.kt`
- `SmsGameActivityArgs`: add `val isTrial: Boolean = false`.
- `SmsGameRoutes`: add `IS_TRIAL_ARG = "isTrial"`; `GAME` gains `&isTrial={isTrial}`;
  `game(chapterCode, isLiveUpdateMode, isTrial = false)`.
- `GameScreenNavigation`: add `navArgument(IS_TRIAL_ARG){ BoolType; defaultValue=false }`;
  collect `viewModel.navigateToBuy` → `onNavigateToBuy()`; pass `isTrial = state.isTrial` and
  `onTrialBuyClick = viewModel::onTrialBuyClicked` to `SmsGameScreen`. Add `onNavigateToBuy` param.
- `SmsGameActivity`: read `args.isTrial`; build the start destination WITH the flag so the nav
  `SavedStateHandle` carries it — `startDestination = SmsGameRoutes.game(chapterCode,
  isLiveUpdateMode = false, isTrial = args.isTrial)`; pass `onNavigateToBuy = { fadeThenRun
  { finish() } }` (returns to GamePreview, which now shows state C: Restart + Buy).

### 4.7 Play ViewModel — `game_play/GameEngineViewModel.kt`, `state/GameUiState.kt`
- `GameUiState`: add `val isTrial: Boolean = false`.
- Read `isTrial` from `SavedStateHandle` (`SmsGameRoutes.IS_TRIAL_ARG`, default false); set it in
  `init` (`:100‑108`).
- Add `private val _navigateToBuy = Channel<Unit>(BUFFERED)` + `navigateToBuy` flow; add
  `fun onTrialBuyClicked() { _navigateToBuy.trySend(Unit) }`.
- `handleEffect` / `onNextChapterClicked` unchanged (in trial we simply never show the
  next‑chapter CTA, so navigation to chapter 2 is blocked at the UI layer).

### 4.8 Chapter‑end UI — `mapper/MessageItemMapper.kt`, `SmsGameScreen.kt`
- `Message(...)`: add `isTrial: Boolean = false` and `onTrialBuyClick: () -> Unit = {}`.
  `ChapterEnd` branch (`:68‑76`): if `isTrial` → `MessageNextChapter(
  title = stringResource(R.string.message_trial_finished_title),
  buttonText = stringResource(R.string.message_trial_finished_button),
  showButton = true, onClick = onTrialBuyClick)` else the existing `MessageNextChapter`.
- `SmsGameScreen`: add a fade wrapper `handleTrialBuyClick` (mirror `:93‑101`) and pass
  `isTrial = state.isTrial`, `onTrialBuyClick = handleTrialBuyClick`.

### 4.9 Strings — `game/presentation/src/main/res/values/strings.xml` (+ `values-fr`, etc.)
`game_menu_try_first_chapter` = "Try";
`game_menu_try_first_chapter_subtitle` = "Chapter 1";
`message_trial_finished_title` = "Trial finished";
`message_trial_finished_button` = "Buy the story". Add to every locale.

## 5. End‑to‑end flow

1. User opens a paid, unowned SMS game at chapter 1 → `Purchase(1, showTry=true)` → **Buy + Try**.
2. Tap **Try** → `OnTry` → (nickname if needed) → `PlayGame(..., isTrial=true)` →
   `SmsGameActivity` launched with `chapterCode=1A`, `isTrial=true`.
3. User plays chapter 1. Engine reaches the chapter boundary → persists chapter 2, appends the
   `ChapterEnd` message, sets `ChapterFinished` (unchanged engine behavior).
4. `MessageItemMapper` sees `state.isTrial` → renders **"Trial finished / Buy the story"** instead
   of `MessageNextChapter`. The next‑chapter channel is never used → user cannot reach chapter 2.
5. Tap **Buy the story** → `onTrialBuyClicked` → `navigateToBuy` → `finish()` → back to
   GamePreview. Progress is now chapter 2 and still unowned → `Purchase(2, showTry=false)` →
   **Buy + Restart** (state C). User buys or restarts.
6. After purchase → `isPurchased` flips (reactive) → gate falls through to Download/Play at
   chapter 2 (resume). Restart clears progress → back to state A.

## 6. Ownership & global premium

A global‑premium subscriber must see the game as owned (no Try/Buy). Implement by folding
`observeHasGlobalPremium()` into `isPurchased` (§4.1). This also corrects the current label‑only
use of premium. Covered by a new test; default `FakePurchaseRepository.hasGlobalPremium = false`
keeps existing tests green.

## 7. Scope & edge cases

- **Friendzoned games** (legacy id in `159..163`, routed by app `FriendzonedGameRouter`)
  use a dedicated loader with no trial support → **Buy‑only** (`showTry=false`).
- **Other legacy games** (`legacyId != null` but not in `159..163`) still run on the SMS
  engine and **keep the Try entry** — `legacyId != null` alone must not suppress it.
- **Nickname prompt** already keys on chapter 1 → reused as‑is for the trial entry.
- **Re‑entry:** trial state is recomputed from progress + ownership each time; no persisted trial flag.
- **Debug node jumps / live‑update mode** are unaffected (`isTrial=false` in those paths).
- **Client‑side only**, consistent with existing `Chapter.isAvailable` checks. Server‑side
  enforcement is out of scope.

## 8. Test plan (run debug, no cache)

Preserve all existing tests. Add:

1. `toGameActionState` (new): free→Play/Download; paid+owned→Download/Play;
   paid+!owned+ch1→`Purchase(1, showTry=true)`; paid+!owned+ch2→`Purchase(2, showTry=false)`;
   friendzoned(159) paid+!owned+ch1→`Purchase(1, showTry=false)`;
   legacy-but-not-friendzoned(999) paid+!owned+ch1→`Purchase(1, showTry=true)`;
   global‑premium treated as owned.
2. `toButtonsState` (new): `Purchase(1, showTry=true)`→left Try(`OnTry`)/right Buy;
   `Purchase(2, showTry=false)`→left Restart(`OnRestart`)/right Buy.
3. `GamePreviewViewModel`: `OnTry`→`PlayGame(isTrial=true, chapterCode="1a")`;
   `OnPlay`→`isTrial=false`; global‑premium → `isPurchased=true`.
4. `GameEngineViewModel`: `isTrial` read from `SavedStateHandle` → `uiState.isTrial`;
   `onTrialBuyClicked`→`navigateToBuy` emits; `onNextChapterClicked` unchanged.

## 9. Sequencing (each step keeps the build green)

1. Verify `memory.save()` at `ChangeChapter` persists `UserGameProgress` to chapter 2 (read‑only).
2. §4.1 ownership consistency + test.
3. §4.2 `Purchase(chapterNumber, showTry)` + `GameItem.legacyId` + mapper tests.
4. §4.3 buttons + `OnTry` + strings + button tests.
5. §4.4 `OnTry`/`navigateToPlay(isTrial)`/`PlayGame` fields + VM tests.
6. §4.5 MainActivity threading of `chapterCode`/`isTrial`.
7. §4.6 args/route/activity wiring + `onNavigateToBuy`.
8. §4.7 GameEngineViewModel `isTrial`/`navigateToBuy` + tests.
9. §4.8 mapper/SmsGameScreen swap + §4.9 strings.
10. Build `:game:presentation:assembleDebug` and `:app:assembleDebug` with `--no-build-cache`;
    run the new + existing unit tests; manual sanity on a paid SMS game (states A → B → C → buy).

## 10. Product decisions — CONFIRMED

- **Global premium = owned** (§6).
- **Trial‑finished "Buy" → return to preview** (`finish()`; no purchase deep‑link).
- **Friendzoned (legacy id 159..163) = Buy‑only** (`showTry=false`). Non‑Friendzoned legacy
  games keep Try.

## 11. Team review (Leland / Uncle Bob / Holzmann / Liskov / Romain / Simon Brown)

Verdict: **sound, minimal, ship it** — one correction applied (§4.6).

- **Leland (Android core) — caught a real gap.** `GameEngineViewModel` reads `isTrial` from the
  *nav* `SavedStateHandle`, which is populated from the route URI — **not** from the activity
  intent extra. So `isTrial` must ride in the start‑destination URI, not only in
  `SmsGameActivityArgs`. §4.6 now builds `startDestination = SmsGameRoutes.game(chapterCode,
  isLiveUpdateMode = false, isTrial = args.isTrial)`. The intent extra is kept as the single value
  passed from MainActivity; both route and extra survive process recreation.
- **Holzmann (simplicity).** Approves the single gate + progress‑driven state C (no new store).
  One dependency to respect: state C relies on the engine persisting the next chapter at the
  boundary (`GameEngine.kt:451‑452`). Mitigated by the §9 step‑1 verification and the new
  `Purchase(2) -> Restart` test; add an invariant comment in `toGameActionState`.
- **Liskov (contracts).** Folding global premium into `isPurchased` changes its meaning from
  "owns this SKU" to "has full access". Only the preview's local computation changes;
  `CreateViewModel.kt:235` and `AccountViewModel.kt:48` compute ownership independently and are
  intentionally left for a follow‑up (same latent inconsistency, out of scope). `PlayGame` new
  fields have defaults → source/binary compatible.
- **Uncle Bob / Simon Brown.** Engine↔presentation boundary preserved; trial is a client‑side
  product gate, not a security boundary. `showTry` is derived in exactly one place
  (`toGameActionState`); the button renderer stays dumb.
- **Romain (rendering).** Reusing `MessageNextChapter` adds no component/measure cost;
  `state.isTrial` is stable for the session, so no extra recompositions.

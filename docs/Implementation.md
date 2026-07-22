This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch for that work and switch on it. And to commit between each
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

# Task 1 - Investigate on MessagesCoinsDialogComposable and BuyTokensDialogViewModel.

1. What are the algorithmic problems?
2. Why it infinite loads?

---

## Task 1 - Status: DONE (branch `ai-conversation-investigation`) - Investigation report

### 2. Why it infinite loads (root cause)

`BuyTokensDialogViewModel` is an **empty shell**: it declares 5 pieces of state but only ever
writes to one (`isVisible`).

- `_state` is initialized to `BuyTokensDialogState.Loading` and **no code path anywhere in the
  codebase assigns any other value**. `BuyTokensDialogComposable` renders `LoadingComposable()`
  (spinner) for that state -> permanent spinner.
- `_coinsState` is initialized to `BuyTokensCoinsDialogState.Loading(-1)` and never becomes
  `Loaded` -> the coin row spins forever and leaks the `-1` sentinel into the UI ("-1 coins").
- The only flow collected is the visibility flow (`ObserveMessageCoinsDialogVisibilityUseCase`),
  which only toggles `isVisible`. Nothing loads message packs, the coin balance, or the login
  state. `onResume()` is empty. `savedStateHandle` is injected but unused.
- The domain layer already provides everything needed (`GetAiMessagesPacksUseCase`,
  `GetAiTokensStateUseCase`, `ObserveAiTokenStateUseCase`, `TryMessagePackUseCase`,
  `AiConversationShopRepository`) - the ViewModel simply never calls any of it.

Conclusion: the feature is a WIP skeleton - UI states + composables were built, the ViewModel
logic (load packs, observe balance, login gate, buy flow) was never implemented.

### 1. Algorithmic / structural problems

**CRITICAL**
- C1. No state machine: `state`/`coinsState`/`titleState`/`messagesPacks` are write-never
  fields; the dialog can never leave `Loading` (see above).
- C2. Double source of truth for packs: `BuyTokensDialogState.Packs` carries
  `packs: List<UiMessagePack>`, but the composable ignores `state.packs` and renders
  `viewModel.messagesPacks.value` (a separate `List<AiMessagePack>`). The state payload is dead
  code; two parallel lists of different types must be kept in sync manually (Liskov contract
  broken: the state no longer describes what the UI renders).
- C3. Broken sealed hierarchy: `BuyTokensDialogState.Error.NotEnoughCoins` is *nested inside*
  `Error` but extends `BuyTokensDialogState` directly, not `Error`. Any `is Error` check silently
  misses `NotEnoughCoins`. In the composable's `when`, the `Error` branch precedes the
  `NotEnoughCoins` branch - it only works by accident of the broken hierarchy; making
  `NotEnoughCoins` a real `Error` subtype would silently change behavior (generic error UI
  instead of the shop CTA).

**MAJOR**
- M1. All user actions are no-ops: buy, cancel, try, "open shop", confirm-buy buttons are
  `// TODO` or empty lambdas; `onClickLogin` in `MessagesCoinsDialogComposable` is `{}`.
  Every interactive element in the dialog is dead.
- M2. Edge-triggered visibility over a lossy channel: `AiConversationCoordinatorImpl.events` is
  `MutableSharedFlow(replay=0, extraBufferCapacity=1, DROP_OLDEST)` written via non-suspending
  `tryEmit`. If the collector is not yet active (or busy) the open event is dropped -> dialog
  never opens, no error. Visibility is modeled as events instead of a state, so it also cannot
  be restored after observer restart.
- M3. Public `MutableState`s: all 5 VM properties are exposed as `MutableState` - any composable
  can mutate VM state; encapsulation broken.
- M4. Implicit double-`hiltViewModel()` coupling: `MessagesCoinsDialogComposable` and the inner
  `BuyTokensDialogComposable` each call `hiltViewModel()` and rely on resolving to the same
  owner to share one instance. Move one into a different `ViewModelStoreOwner` (e.g. a nav
  destination) and the dialog silently breaks (two instances: one receives visibility, the
  other renders).

**MINOR**
- m1. `Loading(-1)` sentinel rendered as a real amount ("-1").
- m2. `BuyTokensDialogComposable` renders `subtitle = "TODO"` for pack buttons.
- m3. `BuyDialogDivider` applies `.alpha(0.1f)` on an already 0x11-alpha color (invisible).
- m4. `executeFlowUseCase` collects the infinite visibility flow on `Dispatchers.IO` and hops
  to Main per event (harmless here, but a `stateIn`/`collectLatest` on Main would be simpler).

### Task 2 - Status: DONE - Fix implementation

Amendments agreed with the panel: Liskov contract fixes first, coordinator fix in its own
commit, dead actions removed rather than shipped (Norman's gate).

- **Sealed hierarchy (C3)**: `Confirm` is now a real `BuyTokensDialogState` subtype;
  `Error` is a sealed class with `Generic` + `NotEnoughCoins` subtypes (`is Error` now
  matches both). `Confirm.Try` removed: the Try flow is owned by the home screen
  (`AiConversationHomeViewModel.consumeTryPack`), the dialog's Try UI was dead code.
- **Double source of truth (C2)**: `messagesPacks` deleted from the VM; the composable renders
  `BuyTokensDialogState.Packs.packs` (single source). `UiMessagePack.price` is now nullable
  (billing may be unavailable); title is derived from `state` in the composable instead of a
  write-never `titleState` field.
- **State machine (C1)**: VM now loads on dialog open - login gate (`Login` state),
  `GetAiMessagesPacksUseCase` + batched `PurchaseRepository.queryProductDetails` for prices,
  balance observed via `ObserveAiTokenStateUseCase` (`Loaded` coin count, no more `-1`
  sentinel spinner). States reset on close so each open reloads fresh data.
- **Buy flow**: pack -> `Confirm.Buy` -> `PurchaseRepository.purchase(sku)` -> `Success`.
  Backend credit is delivered by the new `AiMessagePackPurchaseBackendRegistrar`
  (`:ai-conversation:data`, `@IntoSet` multibinding, same pattern as
  `GamePurchaseBackendRegistrar`): the purchase module's coordinator retries
  `buyMessagePack` with backoff, and the credited balance reaches the dialog through
  `observeAiTokenStateUseCase`. Pack identifiers are cached in SharedPreferences
  (`getCachedMessagePackIdentifiers`) so `supports(sku)` needs no network call.
  Cancelled/pending purchases return silently to the pack list; other failures show
  `Error.Generic`.
- **Dead actions (M1)**: all buttons wired (pack click, confirm buy, cancel, backdrop close).
  `onClickLogin` closes the dialog then `OpenSignInPageUseCase`. The `NotEnoughCoins` shop CTA
  composable was removed (unreachable state, dead button); `NotEnoughCoins` currently renders
  as a generic error.
- **Encapsulation (M3/M4)**: public VM properties are immutable `State<>`; the inner
  `BuyTokensDialogComposable` receives the VM instance from `MessagesCoinsDialogComposable`
  instead of a second `hiltViewModel()` (no more implicit store-owner coupling).
- Unused `savedStateHandle` and empty `onResume()` removed from the VM.

Validation: `:ai-conversation:presentation:assembleDebug`, `:ai-conversation:data:assembleDebug`,
`:app:assembleDebug` (Hilt aggregation) - all green, `--no-build-cache`.

- Task 3 (optional, low priority): make coordinator visibility level-triggered
  (`MutableStateFlow`) to eliminate the dropped-event race.

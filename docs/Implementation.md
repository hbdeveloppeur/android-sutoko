This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch for that work and switch on it. And to commit between each
task - you can compact your context between each task.

# Task 1 - Investigation of :ai-conversation:* modules

These have been made buy our junior team so it may have bugs. You are allowed to do whatever you
want to limit tests, you can curl, use scripts, libraries, etc.

1. Deep analyze the modules.
2. Update this Implementation file with your plan task by task.
3. Start with task 1

Make a plan on investigating the modules.

---

## Task 1 - Status: DONE (branch `ai-conversation-investigation`)

### Investigation method

Full read-only audit of the 4 modules (345 Kotlin source files): `:ai-conversation:core` (13),
`:ai-conversation:data` (100), `:ai-conversation:domain` (102), `:ai-conversation:presentation`
(130). Every ViewModel, repository, websocket component, DAO, and domain entity was read in full;
DTOs/layout composables skimmed for anomalies. Findings were verified against real consumers
(app DI, screens, `Server.urlPrefix()`).

Result: **14 CRITICAL**, **~35 MAJOR**, ~60 MINOR findings. Resource naming and
CancellationException rules are globally respected (only `MicrophoneRepositoryImpl.startRecording`
breaks the Result-contract, see T6).

### CRITICAL findings (must fix)

| # | File | Finding |
|---|------|---------|
| C1 | `data/.../AiConversationShopRepositoryImpl.kt:89-99` | `buyMessagePack`: success falls through to error mapping → **purchase always reported as failure** |
| C2 | `data/.../ConversationRepositoryImpl.kt:222-243` | `selectChoice` flow **never emits** → `.first()` crashes |
| C3 | `data/.../MessageQueueImpl.kt:86-98` | `mark()` discards the `copy()` → total **no-op**, queue states never update |
| C4 | `data/.../WebSocketDataSourceImpl.kt:106-241` | Parser exceptions **uncaught on OkHttp thread** → crash on any unknown server message |
| C5 | `data/.../WebSocketDataSourceImpl.kt:268-270` | `send()` is a **no-op stub** → pong keepalive never sent, server drops connection |
| C6 | `data/.../NewCharacterMessageHandlerImpl.kt:35-38` + `presentation/.../RemoteAssetsUrl.kt:5-7` | `replace("//","/")` corrupts `https://` → **all relative asset URLs broken** |
| C7 | `presentation/.../ConversationViewModel.kt:750-763` | `viewModelScope.launch` inside `onCleared()` never runs → **queued messages lost** |
| C8 | `presentation/.../AiConversationHomeViewModel.kt:230-237` | `first{}` duplicated outside try/catch → `NoSuchElementException` crash |
| C9 | `presentation/.../ImageGeneratorViewModel.kt:108-110` | `?: throw IllegalStateException()` on resume when logged out → crash |
| C10 | `presentation/.../InviteCharacterViewModel.kt:51-56` | Uncollected `WhileSubscribed` user flow → **invite feature dead** (empty list) |
| C11 | `presentation/.../RequestRealTimePreviewViewModel.kt:46-103` | Same pattern → **delete image never works** ("not connected" toast) |
| C12 | `presentation/.../DocumentsRowViewModel.kt:40-44` | `observeConnection()` collects forever → `observeDocuments()` **unreachable**, row always empty |
| C13 | `domain/.../MessageVocal.kt:19` | Super receives a **different random UUID** → two `id` fields; Gson crash / identity bug |
| C14 | `domain/.../Message.kt:53,68-78` | `copy()` **erases `MessageImage.description`** on every state transition → description lost |

### Fix plan (task by task, one commit per task)

Status: **tasks 2-12 DONE** (commits `4fe418a4`..`596892e2`, `:app:assembleDebug` verified).
Remaining known MINORs (not fixed, low value/risk): `BounceClick` (unused), `BuyTokensDialog`
WIP feature, artificial `delay()`s, `Message.copy()` open-class else-throw, DAO blocking writes,
public `MutableState`s in some image-generator VMs, `isLastReceivedMessage` O(n) lookup,
`SecondCounter` 60fps updates.

- **Task 2 — URL & crash hotfixes (C6, C8, C9, C4).** ✅ Fix `replace("//","/")` (both call sites:
  join prefix/path safely instead). Guard `first{}` in home VM, replace throws in
  `ImageGeneratorViewModel` with graceful logged-out state. Wrap websocket `onMessage` parsing in
  try/catch so unknown messages never crash the OkHttp thread.
- **Task 3 — Websocket integrity (C5 + related MAJORs).** Implement real `send()`/`sendPong()`,
  honor `WebSocket.send()` boolean, emit failure when socket is null, drop `null` texts from
  payload, make `webSocket`/`_isConnected` `@Volatile`, close old socket on reconnect, close flow
  on failure, remove debug prints, shut down the OkHttpClient.
- **Task 4 — Dead ViewModels (C10, C11, C12).** Collect the user flow (or use suspend
  `observeUser().first()` where already used elsewhere), split the two infinite collects into
  separate coroutines in `DocumentsRowViewModel`, make `loadCharacters()` retry `getCharacters()`.
- **Task 5 — Conversation data correctness (C2, C3, C14, C13 + MAJORs).** Emit from
  `selectChoice`; fix `MessageQueueImpl.mark` to keep the copy; forward `description` in
  `Message.copy()`; pass `id` through in `MessageVocal`; fix `addMessage(s)` ordering/atomic
  `update {}`; stop swallowing API errors with `printStackTrace`; surface the mapped `ApiException`
  in `sendMessageMedia`.
- **Task 6 — Money & user-data safety (C1 + MAJORs).** Fix `buyMessagePack` fall-through; handle
  null body in `tryMessagePack`; don't delete the source file on failed upload; fix
  `FileManagerRepositoryImpl.getTmpCopy` returning the original; fix MediaStore ghost rows and
  wrong MIME for recordings; catch all exceptions + always release in `MicrophoneRepositoryImpl`.
- **Task 7 — Message lifecycle & reconnect (C7 + MAJORs).** Replace `onCleared` send with a
  proper flush (non-cancelled scope or skip); fix WS reconnect loop (cancel previous collector,
  backoff); rollback optimistic coin decrement on failure; exhaustive error `when`.
- **Task 8 — Domain equality contracts (Liskov).** Harmonize `Message` base vs data-class
  `equals`/`hashCode` (`hiddenState` vs `isAcknowledged`); fix `AiCharacter` equals(id) vs
  hashCode(all-fields) violation; fix `SendMessageUseCase` vocal path silently dropping
  images/narrations; unify ms-vs-seconds timestamp defaults in message deserializers.
- **Task 9 — Image generation & media repositories.** Emit on all `delete`/`describeMedia`/
  version-check paths; atomic StateFlow updates; apply the discarded sort in `createNewDocument`;
  rollback optimistic PROCESSING request on failure; `!!` removal in
  `ImageGenerationRequestMessageHandlerImpl` + propagate failures to the repository.
- **Task 10 — Core module (coordinator, image downloader, permissions, dates).** Fix unchecked
  `tryEmit` (use `emit` or `tryEmit`-with-check) in `AiConversationCoordinatorImpl`; legacy
  storage branch or `minSdk`-aware `ImageDownloaderImpl` + delete-on-failure (ghost images);
  align `PermissionCheckerImpl` with scoped storage; resolve `DateUtils` seconds/ms mismatch.
- **Task 11 — Presentation polish (MAJOR/MINOR batch).** `AddCharacterViewModel` nav event
  `replay=0`; `ConversationListTransformer` swapped First/Last positions; record button
  `LaunchedEffect` key on `size.value`; composition side effect in `InviteCharacterComposable`;
  private-ize exposed `MutableState`s; `BuyTokensDialog` decision (implement or hide WIP);
  spinner/loading-state fixes (`SettingsScreenViewModel`, home `onFailure`); `CharactersGrid`
  columns≥1 guard; "null"/"-1" text renderings.
- **Task 12 — Build hygiene.** Declare real Compose deps in `:ai-conversation:core`; drop unused
  appcompat/material/hilt-work/compose from `:ai-conversation:domain`; dedupe deps in
  `:ai-conversation:presentation`; remove hardcoded VERSION_CODE/NAME.

Validation per task: `./gradlew :ai-conversation:<module>:assembleDebug --no-build-cache` plus
targeted unit tests where the module has a test source set.

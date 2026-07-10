# Android — CinematicScreen integration (SmsGame)

This document specifies the **CinematicScreen** feature for the unified SmsGame player
(`game/` modules). A *cinematic* is a short, non-interactive, linear sequence embedded inside
an SMS chapter: a black screen with full-bleed scenes, ambient sound, and sentences that fade in
and out one after another. When it ends, the SMS conversation continues exactly where it was
interrupted.

The feature is described in three parts: **When** it happens, **What** it is, and **How** it runs.
It then covers the data / domain / presentation changes, edge cases, testing, and a recommended
implementation order.

**Audience:** the Android developer implementing the feature. It is written against the current
codebase (`game/data`, `game/domain`, `game/presentation`) and explicitly calls out what already
exists versus what must be added. Legacy `games/friendzone*/textcinematic` is referenced only as
the behavioral origin (see §16).

**Guiding principles (the team):** keep the engine dumb and the orchestration in the ViewModel
(Uncle Bob / dependency rule); make every boundary contract explicit and fail fast on bad graphs
(Liskov); one simple linear walk, no recursion, bounded complexity (Holzmann / JPL); reuse the
existing fade, scene and sound paths instead of inventing a parallel stack (Leland / Romain); a
clear "what lives where" structure across data → domain → presentation (Simon Brown).

---

## 1. User-visible behavior

- The player is reading an SMS chapter in `SmsGameActivity` / `SmsGameScreen`.
- The conversation **fades to black**, a cinematic plays (scenes + sound + fading sentences), then
  the screen **fades back** to the conversation and the story resumes.
- The cinematic is **non-interactive**: it auto-advances. There is no tap-to-skip (matches legacy).
- The switch `SmsGameScreen ↔ CinematicScreen` is a smooth black cross-fade ("filter" fade), never a
  hard cut.

---

## 2. Terminology

| Term                      | Meaning                                                                                             |
|---------------------------|-----------------------------------------------------------------------------------------------------|
| **Cinematic**             | The linear, non-interactive sequence played between two markers.                                    |
| **StartingCinematicNode** | A node of `type = "code"` whose sentence is `[intro=start]`. It opens the cinematic.                |
| **EndingCinematicNode**   | A node of `type = "code"` whose sentence is `[intro=end]`. It closes the cinematic.                 |
| **Cinematic body**        | The nodes strictly between the start and end markers. Only these are rendered by `CinematicScreen`. |
| **Resume node**           | The single successor of the EndingCinematicNode. The SMS engine resumes here.                       |

A **"code" node** is a legacy authoring concept (a node that carries a bracketed command such as
`[intro=start]`). In the modern graph it is a **new node type** that must be added (see §6).

> Naming note: the draft calls the bracket string a "sentence". The modern DTO
> (`NodeDataDto`) has **no `sentence` field** — it has `text` and `label`. The bracket string is
> carried in `data.text`. Throughout this doc, "sentence" for a code node means `NodeDataDto.text`.

---

## 3. When — detection and trigger

A cinematic starts when the `GameEngine`, during normal linear traversal, reaches a
StartingCinematicNode (`type = "code"`, `text == "[intro=start]"`).

Detection lives in the **engine/handler layer**, not in the UI:

1. The engine dispatches the current node to its handler (`GameEngine.getHandler(node)`).
2. The `CodeNodeHandler` recognizes `[intro=start]`, finds the matching `[intro=end]`, and emits a
   single effect `HandlerEffect.EnterCinematic(startNodeId, endNodeId)`.
3. The handler's script **does not navigate forward** — the engine parks at the start marker until
   the presentation layer resumes it. The body nodes are **never** executed as SMS.

The engine is linear by default and only branches on SMS choices
(`NodeResolver.resolveNextNode` → `AwaitChoice` when ≥2 outgoing edges target `Node.Message`), so a
cinematic body authored with only `scene-node` / `sound` / `intro-sentence` is naturally linear.

---

## 4. What — the cinematic graph and its nodes

The cinematic is the **linear sub-graph** between the start and end markers (both excluded).

`CinematicScreen` can render **only** the following node types:

| Node `type`                | Domain variant               | Rendered as                                                               |
|----------------------------|------------------------------|---------------------------------------------------------------------------|
| `scene-node`               | `Node.Scene(sceneId: Int)`   | Full-bleed background (image/video/color), reused from `SceneComposable`. |
| `sound`                    | `Node.Sound(soundUrl, loop)` | Ambient sound effect, reused from the engine's `PlaySound` path.          |
| `intro-sentence` (**new**) | `Node.IntroSentence(...)`    | A line of text that fades in, stays, then fades out.                      |

### 4.1 `intro-sentence` (new node type)

| Field       | Type        | Meaning                                                              |
|-------------|-------------|----------------------------------------------------------------------|
| `text`      | `String`    | The sentence to display, with a smooth fade in / fade out.           |
| `alignment` | `String`    | Where it appears: `start` \| `end` \| `top` \| `bottom` \| `center`. |
| `delay`     | `Long` (ms) | Time before the sentence appears (legacy "delay before appear").     |
| `duration`  | `Long` (ms) | How long the sentence stays fully visible (legacy "stay").           |

The fade itself is a fixed spec (see §10.3); `delay` and `duration` are the per-sentence timing.

### 4.2 Graph constraints (validated at extraction)

A cinematic body **must** be linear: every node in it has exactly one outgoing edge, and the chain
from the start marker reaches the end marker with no branching, no cycle, and no dead end. Anything
else is an authoring error and is handled per §12. Unknown node types inside the body are ignored by
the parser today (see §6) — for cinematics they must be rejected explicitly instead.

---

## 5. How — end-to-end flow

```
SmsGameScreen (engine traversing)
        |
        | reaches [intro=start]
        v
CodeNodeHandler -> EnterCinematic(start, end)
        |
        | ViewModel: pause engine, extract body, navigate
        v
CinematicScreen (plays scene-node / sound / intro-sentence)
        |
        | body exhausted (or cancelled)
        v
ViewModel: engine.startFromNode(resumeNode = successor of [intro=end])
        |
        v
SmsGameScreen (conversation continues)
```

1. **Detect & park.** `CodeNodeHandler` emits `EnterCinematic` and the engine stops advancing.
2. **Pause.** The ViewModel pauses the engine (so no background traversal/effects leak during the
   cinematic) and extracts the linear body from the current `ChapterGraph`.
3. **Hand off.** The ViewModel publishes the body to a shared, activity-scoped state and navigates
   to `CinematicScreen` (black cross-fade).
4. **Play.** `CinematicScreen` walks the body in order: scenes, sounds, and fading sentences.
5. **Resume.** When the body is exhausted (or the cinematic is cancelled), the ViewModel resumes the
   engine at the **resume node** (successor of `[intro=end]`) via `startFromNode`, and pops back to
   `SmsGameScreen` (black cross-fade).

The engine stays responsible for SMS traversal only; the ViewModel orchestrates the cinematic
interlude. This keeps the engine's contract small and the cinematic logic testable in isolation.

---

## 6. Data & domain model changes

The following are **additive**. Unknown node types are silently dropped by the parser today
(`ChapterGraphParser.parseNode` → `else -> null`); cinematics must not rely on that behavior.

### 6.1 DTO (`game/data/.../local/dto/NodeDto.kt`)

`NodeDataDto` already carries `text` and `label`. For `intro-sentence`, add the three
timing/placement
fields (all optional with defaults so existing nodes are unaffected):

```kotlin
data class NodeDataDto(
    val label: String? = null,
    val text: String? = null,        // code-node sentence: "[intro=start]" / "[intro=end]"
    // ... existing fields ...
    val alignment: String? = null,   // intro-sentence: start|end|top|bottom|center
    val delayMs: Long? = null,       // intro-sentence: delay before appear
    val durationMs: Long? = null,    // intro-sentence: stay duration
)
```

### 6.2 Parsers — both of them

There are **two** parsers that must stay in sync (Liskov: same JSON → same model everywhere):

- `game/data/.../local/parser/ChapterGraphParser.kt` (`parseNode`, `when (dto.type)`).
- `game/data/.../graph/testing/TestChapterGraphLoader.kt` (`parseNode`) — the real-time testing
  path.

Add branches to **both**:

```kotlin
"code" -> dto.toCodeNode()                  // Node.Code(id, sentence = data.text)
"intro-sentence" -> dto.toIntroSentenceNode() // Node.IntroSentence(...)
```

### 6.3 Domain model (`game/domain/.../model/chapter/Node.kt`)

Add two variants to the sealed `Node` class:

```kotlin
data class Code(override val id: String, val sentence: String) : Node() {
    val isIntroStart: Boolean get() = sentence.trim() == "[intro=start]"
    val isIntroEnd: Boolean get() = sentence.trim() == "[intro=end]"
}

data class IntroSentence(
    override val id: String,
    val text: String,
    val alignment: Alignment,   // mapped from the JSON string at parse time (§10.2)
    val delayMs: Long,
    val durationMs: Long,
) : Node()
```

`Alignment` is a **domain** enum (`START, END, TOP, BOTTOM, CENTER`) mapped once at the boundary;
the
UI maps it to a Compose `Alignment` (§10.2). Parsing validates the string and fails fast on an
unknown value (§12).

Add the matching `NodeType` entries (`CODE`, `INTRO_SENTENCE`) in `engine/NodeType.kt`.

### 6.4 Handler & wiring

- New `CodeNodeHandler` (`game/domain/.../engine/handlers/`) implementing
  `NodeHandler.buildScript(node, memory)`. On `[intro=start]` it emits
  `HandlerEffect.EnterCinematic(startNodeId, endNodeId)` and returns a script that **does not**
  navigate (engine parks). On `[intro=end]` it is a no-op (the end marker is never reached during
  normal play because the body is skipped; it only exists as the resume anchor).
- `intro-sentence` has **no engine handler** — it is consumed only by `CinematicScreen`, never by
  the
  SMS engine. It must therefore not appear on a path the SMS engine traverses (extraction guarantees
  it lives only inside a cinematic body).
- Wire `Node.Code` → `NodeType.CODE` in `GameEngine.getHandler(node)` (lines ~505‑520) and in
  `NodeHandlerFactory`. `Node.IntroSentence` is intentionally **not** wired into the engine.

### 6.5 Effect

Add to `HandlerEffect` (`game/domain/.../engine/HandlerEffect.kt`):

```kotlin
data class EnterCinematic(val startNodeId: String, val endNodeId: String) : HandlerEffect()
```

The ViewModel already collects `gameEngine.effects` and dispatches in `handleEffect`; add one
branch.

---

## 7. GameEngine changes (minimal surface)

The engine (`game/domain/.../engine/GameEngine.kt`) already carries a private `isPaused` flag
(checked in `executeNode` / `executeScript`) but exposes **no** public pause/resume and **no** way
to
impose an external node sequence. Add the smallest surface that supports the interlude:

```kotlin
fun pause()   // sets isPaused = true; in-flight handler script may finish, no new node starts
fun resume()  // sets isPaused = false; (used only if the engine parks mid-script)
suspend fun startFromNode(nodeId: String)   // already exists — used to resume at the resume node
```

`startFromNode` already jumps to an arbitrary node and then auto-traverses via edges — exactly the
resume behavior we need. No new "external sequence" API is required (Holzmann: don't add a general
mechanism for one caller). The cinematic body is **never** fed to the engine; it lives only in the
ViewModel/shared state.

Invariants the engine must honor (assert in debug):

- After emitting `EnterCinematic`, the engine does not advance past the start marker until
  `startFromNode(resumeNode)` is called.
- `pause()`/`resume()` are idempotent.
- Cancellation is never swallowed: engine coroutines keep rethrowing `CancellationException`
  (per project rule).

---

## 8. Linear sub-graph extraction

There is **no** existing chain extractor. Build one in `game/domain` (pure function, no Android, no
UI) on top of the raw graph — **not** `NodeResolver`, which applies SMS choice-collapsing logic
(`≥2` message targets → `AwaitChoice`) that is wrong for pure graph extraction.

```kotlin
// game/domain/.../model/chapter/CinematicExtractor.kt (new)
fun extractCinematicBody(
    graph: ChapterGraph,
    startNodeId: String,
    endNodeId: String,
): Result<List<Node>> {
    val body = mutableListOf<Node>()
    val visited = HashSet<String>()
    var current: String? = graph.getNextEdges(startNodeId).singleOrNull()?.target
        ?: return Result.failure(CinematicError.NonLinear("start marker must have exactly one successor"))

    while (current != null && current != endNodeId) {
        if (!visited.add(current)) return Result.failure(CinematicError.Cycle(current))
        val node = graph.getNode(current)
            ?: return Result.failure(CinematicError.MissingNode(current))
        if (!node.isCinematicPlayable)                 // Scene | Sound | IntroSentence
            return Result.failure(CinematicError.UnsupportedNode(current, node::class.simpleName))
        body += node
        val next = graph.getNextEdges(current)
        if (next.size != 1) return Result.failure(CinematicError.NonLinear(current))
        current = next.first().target
    }

    if (current != endNodeId) return Result.failure(CinematicError.EndNotReached(endNodeId))
    return Result.success(body)                        // both markers excluded
}
```

Rules (JPL: one loop, no recursion, bounded by `visited`):

- Follow `getNextEdges(id).singleOrNull()?.target` only. Reject on `size != 1` (branch / dead end).
- Detect cycles with a `visited` set.
- Stop exactly at `endNodeId`; exclude both markers.
- Reject any node that is not `Scene`/`Sound`/`IntroSentence` (e.g. a stray `Message` or `Choice`).
- `resumeNode = getNextEdges(endNodeId).singleOrNull()?.target` (computed once by the ViewModel for
  the resume call).

The extractor returns `Result` (errors are values). Coroutine cancellation is never represented as a
`Result` and always propagates.

---

## 9. Navigation

Follow the existing destination recipe in `game/presentation/.../game_play/`.

### 9.1 Route (`game_play/SmsGameRoutes.kt`)

```kotlin
internal object SmsGameRoutes {
    // ... existing ...
    const val CINEMATIC = "game/cinematic"
    fun cinematic(): String = CINEMATIC
}
```

The route carries **no graph payload** — only primitives are safe as nav args and `Node` is not
`Parcelable`. The body is shared via activity-scoped state (§11).

### 9.2 Destination (`game_play/navigation/CinematicScreenNavigation.kt`, new)

Mirror `GameScreenNavigation.kt` (`NavGraphBuilder.gameScreen(...)`), reusing the standard fade
spec (`tween(..., easing = FastOutSlowInEasing)`):

```kotlin
internal fun NavGraphBuilder.cinematicScreen(
    viewModel: GameEngineViewModel,        // activity-scoped, same instance as gameScreen (§11)
    onExit: () -> Unit,                    // popBackStack after resume
) = composable(
    route = SmsGameRoutes.CINEMATIC,
    enterTransition = { fadeIn(tween(500, easing = FastOutSlowInEasing)) },
    exitTransition = { fadeOut(tween(360, easing = FastOutSlowInEasing)) },
    popEnterTransition = { fadeIn(tween(500, easing = FastOutSlowInEasing)) },
    popExitTransition = { fadeOut(tween(360, easing = FastOutSlowInEasing)) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CinematicScreen(
        body = state.cinematicBody,
        onFinished = { viewModel.onCinematicFinished(); onExit() },
    )
}
```

Register it inside the `SmsGameNavHost { ... }` lambda in `SmsGameActivity.onCreate`, next to
`gameScreen(...)`. `SmsGameNavHost` already applies default fades and a host-wide black overlay
(`overlayAlpha`), so the cross-fade comes for free.

---

## 10. Presentation — CinematicScreen

A full-bleed black screen that plays the body in order. Reuse existing building blocks; do not build
a parallel rendering stack (Romain).

### 10.1 Root & background

```kotlin
Box(Modifier.fillMaxSize().background(Color.Black)) { /* current frame */ }
```

Under `SutokoTheme`, `MaterialTheme.colors.background == Color.Black` and edge-to-edge + transparent
status bar are already enabled, so this matches the SMS screen's black idiom used in
`SmsGameNavHost`/`SmsGameScreen`.

### 10.2 Rendering the body

Drive a single `index` state (no `LazyColumn` — a cinematic has a handful of sequential frames):

```kotlin
when (val node = body[index]) {
    is Node.Scene -> SceneComposable(scene = resolveScene(node.sceneId))  // reuse §10.4
    is Node.Sound -> LaunchedEffect(node) { playSound(node.soundUrl, node.loop) }; advance()
            is Node.IntroSentence -> IntroSentenceLine(node)                              // §10.3
}
```

`Alignment` (domain) → Compose `Alignment` (map once, at the UI edge):

| domain   | Compose `Alignment`      |
|----------|--------------------------|
| `START`  | `Alignment.CenterStart`  |
| `END`    | `Alignment.CenterEnd`    |
| `TOP`    | `Alignment.TopCenter`    |
| `BOTTOM` | `Alignment.BottomCenter` |
| `CENTER` | `Alignment.Center`       |

(Combine vertical + horizontal as needed; the table covers the single-axis values from the spec.)

### 10.3 Sentence fade (`intro-sentence`)

Sequential per line (matches legacy, which swapped one `TextView`): fade **in** → `delay` is the
gap before appear → stay `duration` → fade **out** → next node.

```kotlin
AnimatedVisibility(
    visible = visible,
    enter = fadeIn(tween(FADE_MS, easing = FastOutSlowInEasing)),
    exit = fadeOut(tween(FADE_MS, easing = FastOutSlowInEasing)),
) { Text(node.text, /* Poppins / white, MessageNarration is the closest visual analog */) }
```

- `FADE_MS = 1000` — matches the legacy cinematic fade (see §16); confirmed product decision. Keep it
  a single named constant so it is trivial to retune.
- Timing sequence per line (coroutine, cancellable):
  `fadeIn → delay(node.delayMs) → delay(node.durationMs) → fadeOut`.
- Never swallow `CancellationException` — rethrow before any `catch (Exception)` (project rule).

### 10.4 Scene & sound reuse

- **`scene-node`:** resolve via the existing `GetSceneUseCase` / `SceneRepository` and render with
  the
  existing `SceneComposable` (`ImageBackground` / `VideoBackground` / color filter). Pre-resolve the
  next scene while the current line is showing so the first frame is ready (Romain: avoid jank; no
  per-frame allocation; keep `crossfade` policy consistent with `ImageBackground`).
- **`sound`:** reuse the `PlaySound(soundUrl, loop)` effect contract, but play it on a **dedicated**
  `MediaPlayer` scoped to the cinematic (not the SMS typing/vocal players) so the two never fight.
  Release it in `DisposableEffect.onDispose` / `ViewModel.onCleared` — no leaks across the
  transition.

---

## 11. State hoisting (engine → CinematicScreen)

`GameEngineViewModel` is currently obtained with `hiltViewModel()` inside the destination, which
scopes it to that `NavBackStackEntry` — it dies on navigation and a fresh one is created on return.
That cannot share a payload across two destinations.

**Recommended (as implemented):** keep `GameEngineViewModel` scoped to the **game destination** so
its `SavedStateHandle` still carries `gameId`/`chapterCode`, and share that same instance with
`cinematicScreen` via `hiltViewModel(navController.getBackStackEntry(SmsGameRoutes.GAME))`
(precedent: `hiltViewModel(parentEntry)` in `app/.../MainActivity.kt:315`). `gameScreen` keeps
`hiltViewModel()`; both destinations resolve the game entry's `ViewModelStore`, so they share one
engine and one payload. Navigating to `CINEMATIC` pushes on top of the game destination, so the
scoped VM stays alive for the whole interlude. Add a cinematic slice to `GameUiState`:

```kotlin
data class GameUiState(
    // ... existing ...
    val cinematicBody: List<Node> = emptyList(),
    val isCinematicActive: Boolean = false,
)
```

The ViewModel — which already owns the engine and `currentGraph` — handles `EnterCinematic`:
pause engine → `extractCinematicBody(graph, start, end)` → `resumeNode = successor(end)` → publish
`cinematicBody`/`isCinematicActive` → navigate. On `onCinematicFinished()` →
`engine.startFromNode(resumeNode)` → clear the slice → pop back.

**Fallback:** if activity-scoping the VM is undesirable, mirror the existing `@Singleton`
`StoryLiveUpdateCoordinator` (it already carries `currentGraph` across activity + VM) with a small
`CinematicSessionHolder` set by the VM and read by `cinematicScreen`. Nav args are not an option for
the payload (primitives only).

---

## 12. Transition choreography

Both directions reuse the existing black cross-fade. The host already exposes a `fadeThenRun { }`
helper in `SmsGameActivity` (fade black overlay to 1 → run → fade to 0) and default nav fades.

| Step  | Action                                                              | Spec                                                                         |
|-------|---------------------------------------------------------------------|------------------------------------------------------------------------------|
| Enter | `EnterCinematic` received → pause → extract → `navigate(CINEMATIC)` | nav fade 500/360, optional `fadeThenRun` (500/280/500) for a full black hold |
| Play  | sentences/scenes/sound per §10                                      | sentence fade 1000; scene cross-fade reuses `FILTER_FADE_DURATION` (1200)    |
| Exit  | body exhausted → `startFromNode(resume)` → `popBackStack()`         | nav fade 500/360                                                             |

Keep one source of truth for the fade durations (constants in the screen), consistent with the
project's `tween + FastOutSlowInEasing` idiom. Do not introduce `AnimatedContent` or a
shared-element
layer — none exists in the repo and the fades already cover the brief.

---

## 13. Edge cases & error handling

Validate at the boundary (Liskov), fail fast in debug, degrade gracefully in release.

| Case                                                                            | Handling                                                                                                                                                                                                                                                               |
|---------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[intro=start]` with no matching `[intro=end]`                                  | Extraction returns `CinematicError.EndNotReached`. Log, skip the cinematic, resume normal traversal from the start marker's successor. Never crash.                                                                                                                    |
| Branching / dead end inside the body (`getNextEdges size != 1`)                 | `CinematicError.NonLinear`. Same: log, skip, resume. Cinematics are linear by contract.                                                                                                                                                                                |
| Cycle in the body                                                               | `visited` set detects it → `CinematicError.Cycle`. Log, skip, resume.                                                                                                                                                                                                  |
| Unsupported node in the body (e.g. `Message`, `Condition`, `Choice`)            | `CinematicError.UnsupportedNode`. Log, skip the whole cinematic, resume. Do not half-render.                                                                                                                                                                           |
| Empty body (start immediately followed by end)                                  | Valid no-op cinematic: cross-fade out/in, resume immediately. No frames played.                                                                                                                                                                                        |
| Unknown `alignment` string in JSON                                              | Parser validates at the boundary → fail fast in debug (assert/throw), map to `CENTER` in release with a log.                                                                                                                                                           |
| `type="code"` with a sentence that is neither `[intro=start]` nor `[intro=end]` | Reserved for future codes. Today: ignore (no-op handler) and continue traversal.                                                                                                                                                                                       |
| Back press during the cinematic                                                 | Treat as **cancel**: stop sound, call `startFromNode(resumeNode)`, pop back. The cinematic is non-interactive; back must not exit the chapter (legacy returned `RESULT_CANCELED`, not chapter exit).                                                                   |
| Process death / recreation mid-cinematic                                        | Do **not** persist mid-cinematic state. On recreate, resume the SMS from the last persisted node (accepted limitation — the cinematic is a transient interlude).                                                                                                       |
| Live-update package swap while cinematic active                                 | **Defer** applying a `SEED_UPDATED` graph swap until the cinematic completes — never mutate the graph out from under a running interlude (jarring, and unsafe because body nodes may vanish). On finish, sync to the latest seed, **re-resolve the resume node against the new graph**, then resume; if that node no longer exists, fall back to the chapter start node. Cleanest, least surprising behavior for both player and author. |
| Audio focus loss / screen off                                                   | Pause the cinematic `MediaPlayer` on `ON_PAUSE`, resume on `ON_RESUME` if still active; always release on dispose/clear.                                                                                                                                               |
| Consecutive / nested cinematics                                                 | Out of scope by decision (no nested, no back-to-back). No special re-check on resume. If a resume node ever happens to be `[intro=start]`, the engine simply triggers a fresh cinematic via §3 — no extra logic required.                                                                                                      |

---

## 14. Testing strategy

Build and test in **debug, with no cache** (project rule). Smallest practical test first; remove
throwaway validation code unless it earns its place.

- **Parser (unit):** `ChapterGraphParserTest` + `TestChapterGraphLoaderTest` — round-trip `code` and
  `intro-sentence` JSON into `Node.Code` / `Node.IntroSentence`; both parsers agree; unknown
  `alignment` rejected.
- **Extractor (unit):** `CinematicExtractorTest` — happy path (excludes both markers, preserves
  order), then each error: `EndNotReached`, `NonLinear`, `Cycle`, `UnsupportedNode`, `MissingNode`,
  empty body. Pure functions → fast, deterministic.
- **Engine (unit):** `CodeNodeHandler` emits `EnterCinematic` and parks; `pause()`/`resume()` are
  idempotent; `startFromNode(resume)` continues traversal; `CancellationException` propagates.
- **ViewModel (unit):** on `EnterCinematic`, body is extracted and published and the engine paused;
  `onCinematicFinished()` resumes at the resume node and clears the slice.
- **Compose UI:** `CinematicScreen` renders a `scene-node` / `sound` / `intro-sentence` sequence in
  order; alignment maps correctly; fade spec applied; finishing calls back exactly once.
- **Contract/golden:** one sample chapter JSON containing a cinematic, committed as a fixture,
  parsed
  end-to-end to lock the schema.

---

## 15. Recommended implementation order

Incremental, each step independently shippable and testable:

1. **Model + parsers.** Add `Node.Code`, `Node.IntroSentence`, `NodeType` entries, DTO fields, and
   the
   two parser branches. Unit tests green. (Engine still ignores the new nodes — safe to ship.)
2. **Extractor.** Pure `extractCinematicBody` + `CinematicError` + tests.
3. **Engine surface.** `CodeNodeHandler` + `EnterCinematic` effect + `pause()`/`resume()` + wiring
   in
   `getHandler`/`NodeHandlerFactory`. Engine tests green.
4. **ViewModel orchestration.** Activity-scope the VM, add the `GameUiState` cinematic slice, handle
   `EnterCinematic` (pause → extract → publish → navigate) and `onCinematicFinished()` (resume →
   clear
   → pop). VM tests green.
5. **Navigation + screen.** Route, `cinematicScreen` destination, `CinematicScreen` composable
   (root, body walker, `IntroSentenceLine`, scene/sound reuse). UI tests green.
6. **Transitions & polish.** Cross-fade timings, dedicated cinematic `MediaPlayer` lifecycle,
   back-press cancel, deferred live-update swap.
7. **Fixture + manual run.** Commit a golden chapter JSON; play a real cinematic on device in debug
   with no cache; verify the `SmsGameScreen ↔ CinematicScreen` fades and a clean resume.

---

## 16. Legacy parity (what `games/friendzone*/textcinematic` did)

Preserved deliberately, so the new screen feels like the old one:

- Sentence-by-sentence **fade in → stay → fade out**, auto-advancing, **non-interactive** (no
  tap-to-skip).
- Per-sentence **delay before appear** (legacy `seen`) and **stay** (legacy `wait`) — now `delay` /
  `duration` on `intro-sentence`.
- Full-bleed background under a **black filter** that cross-fades in/out.
- Ambient **background sound** (`"bg"`) tied to focus/lifecycle.
- Completion **handed back** to the flow (legacy: `setResult(RESULT_OK); finish()`; now: resume
  node + pop).

Intentional changes (call them out in review):

- **Fade duration:** kept at the legacy **1000 ms** (fz1–3; fz4 used the platform default ~300 ms).
  Confirmed product decision — not a behavioral change.
- **Alignment:** legacy had one fixed position (single layout `TextView`). Per-line `alignment` is a
  **new** capability.

---

## 17. Non-goals (for this iteration)

- No branching or interactive choices inside a cinematic.
- No tap-to-skip / progress UI.
- No timecoded audio-description track (the fz4 `AudioCinematic` model) — out of scope unless a
  chapter requires it.
- No persistence of mid-cinematic progress across process death (accepted limitation, §13).
- No shared-element / `AnimatedContent` transition layer.

---

## 18. Quick reference

### New / touched types

```text
Node.Code(id, sentence)                 domain  (isIntroStart / isIntroEnd)
Node.IntroSentence(id, text, alignment, delayMs, durationMs)
NodeType.CODE, NodeType.INTRO_SENTENCE
HandlerEffect.EnterCinematic(startNodeId, endNodeId)
extractCinematicBody(graph, start, end): Result<List<Node>>     // both markers excluded
GameEngine.pause() / resume() / startFromNode(resumeNode)
GameUiState.cinematicBody / isCinematicActive
SmsGameRoutes.CINEMATIC / cinematic()
```

### "What lives where" (Simon Brown)

```text
data         NodeDataDto (+alignment/delayMs/durationMs), ChapterGraphParser, TestChapterGraphLoader
domain       Node variants, NodeType, CinematicExtractor, CodeNodeHandler, HandlerEffect, GameEngine.pause/resume
presentation GameEngineViewModel (activity-scoped), GameUiState slice, CinematicScreen, *Navigation.kt
```

### Golden rules

1. The SMS engine **never** traverses the cinematic body; the body is extracted and played by the
   UI.
2. The body is **linear by contract** — validate at extraction, fail fast, never half-render.
3. Both parsers (`ChapterGraphParser` and `TestChapterGraphLoader`) stay in sync for `code` /
   `intro-sentence`.
4. `resumeNode` is always the single successor of `[intro=end]`; resume via `startFromNode`.
5. Reuse `SceneComposable`, the `PlaySound` contract, and the existing black cross-fade — no
   parallel
   rendering stack.
6. Cancellation always propagates (`CancellationException` is never swallowed; never stored in
   `Result`).
7. Keep fade specs as named constants with `tween + FastOutSlowInEasing`; the sentence fade is
   **1000 ms** (matches legacy).

### Confirmed decisions

- **Sentence fade:** **1000 ms** (matches legacy), not 580 ms.
- **Back-press:** **cancel-and-resume** — stop sound, `startFromNode(resumeNode)`, pop back (never
  exits the chapter).
- **Live-update swap during a cinematic:** **defer** until the interlude ends, then re-resolve the
  resume node against the new graph (cleanest, most user-friendly).
- **Nested / back-to-back cinematics:** **not supported**; no resume re-check needed.

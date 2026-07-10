# Node type `manga-page` — implementation spec

> Status: **Draft — awaiting approval before implementation.**
> Scope: add a new authored node `manga-page` that renders as a `MessageManga`
> bubble in the conversation; tapping **Open** shows the page full-screen with the
> authored speech-bubble text composited over the image.

---

## 1. Goal

Authors can place a node of type `manga-page` in a chapter graph. At runtime it:

1. appears in the `SmsGameScreen` message list as a `MessageManga` item (avatar +
   prompt + **Open** button) — the scaffold composable already exists;
2. on **Open**, displays the page image full-screen with each authored message
   drawn on top (manga speech-bubble style), `[prenom]` substituted with the
   hero name, supporting pinch-zoom and back/close.

### Authoring contract (input)

```json
{
  "id": "fxba4BVO3ul-1A-311",
  "type": "manga-page",
  "data": {
    "assetId": 3887,
    "assetFileName": "fc81f14a-484b-43d3-82fd-405774d9f1e3.webp",
    "name": "manga_page_bestfrien",
    "messages": [
      { "sentence": "Au revoir [prenom], sache que tu ne seras jamais seul",
        "size": 28, "x": 74.3, "y": 30.4, "w": 22 }
    ],
    "delay": 0, "duration": 0, "isAutoTiming": true, "isHesitating": false
  }
}
```

- `assetFileName` is the page file inside the story `assets/` folder (same
  resolution rule as `message-image`). `assetId`/`name` are metadata only.
- `messages[*]` coords are **percentages of the image** (`x`,`y` = text center;
  `w` = constrained text width). `size` is text size **in image pixels**.
- `delay`/`duration`/`isAutoTiming`/`isHesitating` are the standard timing
  fields (see §6).

### Data flow

```
authored JSON -> ChapterGraphParser -> Node.MangaPage ->
MangaPageNodeHandler (substitutes [prenom]) -> GameMessageMangaPage ->
MessageItemMapper -> MessageManga (list item) -> Open -> MangaPageScreen overlay
```

---

## 2. Architecture decision: full-screen overlay (recommended)

Two existing patterns exist for "open something from a message":

| Pattern | Used by | Mechanism | Complexity |
|---|---|---|---|
| **A — in-screen overlay** | `message-image` → `ImageViewerOverlay` | `SmsGameScreen` holds local state, renders an overlay `Composable` inside the same `Box`. No route, no VM channel, no parceling. | Low |
| **B — Nav destination** | `EnterCinematic` → `cinematicScreen()` | VM emits on a `Channel`, NavController navigates, VM reused from the GAME back-stack entry. | High |

**Recommendation: Pattern A.** Routes are string-based (`SmsGameRoutes`) and not
type-safe, so passing a list of text overlays across a route would require
parceling or VM state for little benefit. A manga page is a transient, dismissible
view — exactly like `ImageViewerOverlay`. We still name the composable
`MangaPageScreen` to match the product vocabulary; it is simply a full-screen
overlay composable (BackHandler + zoom), not a Nav destination.

Fall back to Pattern B only if a future requirement needs deep-linking or process-
death restoration of an open page (out of scope now). The composable is named
`MangaPageScreen` but is KDoc'd explicitly as an in-screen overlay (not a route)
so future readers aren't misled.

---

## 3. Data model & engine wiring

The engine is exhaustive/compile-time safe (`NodeHandlerFactory.getHandler` is an
exhaustive `when`). Adding a node therefore requires coordinated edits in three
modules. All symbols below already exist unless marked **NEW**.

### 3.1 `:game:domain`

- **`model/chapter/Node.kt`** — add to the sealed `Node`:
  ```kotlin
  data class MangaPage(
      override val id: String,
      val imageUrl: String,                 // resolved absolute path (see §4)
      val assetId: Int? = null,
      val messages: List<MangaMessage>,
      val waitMs: Long = 0,                 // <- data.duration
      val seenMs: Long = 0,                 // <- data.delay
  ) : Node() {
      data class MangaMessage(
          val text: String,                 // raw sentence; [prenom] substituted in handler
          val size: Float,
          val x: Float, val y: Float, val w: Float,
      )
  }
  ```
- **`engine/NodeType.kt`** — add `MANGA_PAGE`.
- **`engine/GameMessageType.kt`** — add `MangaPage`.
- **`engine/message/GameMessageMangaPage.kt`** — **NEW**:
  ```kotlin
  class GameMessageMangaPage(
      id: String,
      val imageUrl: String,
      val messages: List<TextOverlay>,
  ) : GameMessage(id, GameMessageType.MangaPage) {
      data class TextOverlay(
          val text: String, val size: Float,
          val x: Float, val y: Float, val w: Float,
      )
  }
  ```
- **`engine/handlers/MangaPageNodeHandler.kt`** — **NEW**, mirror
  `MessageImageNodeHandler`. Inject `TextProcessor` (already bound; used by
  `MessageNodeHandler`/`InfoNodeHandler`) and substitute `[prenom]` here so the
  UI receives final text:
  ```kotlin
  // Delay(seenMs.coerceAtLeast(520)) -> Emit(AddMessage(GameMessageMangaPage(...)))
  // each message.text = textProcessor.process(rawText, memory.state.value)
  ```
- **`engine/NodeHandlerFactory.kt`** — add constructor param
  `mangaPageHandler: MangaPageNodeHandler` and branch
  `NodeType.MANGA_PAGE -> mangaPageHandler` (the `when` is exhaustive, so the
  build fails until this is done — desired).
- **`engine/message` mapping in `mapper/GameMessageExt.kt`** — `characterId()`
  returns `null` for manga (no character); no change required unless grouping is
  wanted. Leave as-is.

### 3.2 `:game:data`

- **`local/dto/NodeDto.kt`** — extend `NodeDataDto` with `assetFileName: String?`,
  `name: String?`, `messages: List<MangaMessageDto>?`, plus **NEW**:
  ```kotlin
  @Keep data class MangaMessageDto(
      val sentence: String? = null,
      val size: Float? = null,
      val x: Float? = null, val y: Float? = null, val w: Float? = null,
  )
  ```
- **`local/parser/ChapterGraphParser.kt`** — add a branch in `parseNode`:
  ```kotlin
  "manga-page" -> {
      val file = requireNotNull(data?.assetFileName?.takeIf { it.isNotBlank() }) {
          "manga-page node ${dto.id} missing assetFileName" }
      val messages = data.messages.orEmpty().mapNotNull { it.toDomainOrNull() }
      if (messages.isEmpty()) return null           // drop degenerate nodes
      Node.MangaPage(
          id = dto.id,
          imageUrl = resolveImagePath(file, gameId, legacyId, pathProvider),
          assetId = data.assetId, messages = messages,
          waitMs = data.duration ?: 0, seenMs = data.delay ?: 0,
      )
  }
  ```
  `MangaMessageDto.toDomainOrNull()` applies **legacy defaults** for missing
  fields (`size=30, x=1, y=1, w=10`) and returns `null` only when `sentence` is
  blank — preserving authored intent instead of crashing.

### 3.3 `:game:presentation`

- **`components/message/MessageManga.kt`** — already exists; keep as-is, only
  wire `onClick` from the mapper.
- **`mapper/MessageItemMapper.kt`** — add param
  `onMangaClick: (imageUrl: String, overlays: List<GameMessageMangaPage.TextOverlay>) -> Unit = { _, _ -> }`
  and a branch:
  ```kotlin
  GameMessageType.MangaPage -> {
      message as GameMessageMangaPage
      MessageManga(onClick = { onMangaClick(message.imageUrl, message.messages) })
  }
  ```
- **`components/manga/MangaPageScreen.kt`** — **NEW** full-screen overlay (§5).
- **`SmsGameScreen.kt`** — add local state next to `viewerState`:
  ```kotlin
  data class MangaViewerState(
      val imageUrl: String? = null,
      val overlays: List<GameMessageMangaPage.TextOverlay> = emptyList(),
      val isVisible: Boolean = false,
  )
  ```
  pass `onMangaClick = { url, overlays -> mangaState = MangaViewerState(url, overlays, true) }`
  into `Message(...)`, and render `MangaPageScreen(state = mangaState,
  onDismiss = { mangaState = mangaState.copy(isVisible = false) })`.

No new DI module is required: `TextProcessor` is already provided, and
`MangaPageNodeHandler` uses constructor injection picked up by the factory.

---

## 4. Asset resolution

Reuse the exact rule used by images — by **filename**, not `assetId`
(`Node.MessageImage.assetId` is already never populated). `resolveImagePath`
(`ChapterGraphParser`) yields:

```
<filesDir>/games/<legacyId ?: storyId>/assets/<assetFileName>
```

via `GamePathProvider.getStoryDirectoryPath` / `AndroidGamePathProviderImpl`.
The manga page uses the same call, so downloaded/legacy story assets resolve
identically to `message-image`.

---

## 5. Rendering the page (`MangaPageScreen`)

- Load with **Coil** (`AsyncImage` + `ImageRequest`, `crossfade(300)`) — same as
  `MessageImage`/`ImageViewerOverlay`. Coil sizes the request to the composable's
  constraints by default, so a large webp is down-sampled to the page box.
- Pinch-zoom with `net.engawapg.lib.zoomable` (already used by
  `ImageViewerOverlay`); `BackHandler` + a Close affordance to dismiss.
- The image and the text `Canvas` are siblings inside the **same zoomed page box**, so
  both transform together and the bubbles stay glued to the page under zoom/pan — a
  single source of truth for the coordinate mapping.
- Draw text with Compose `Canvas` + `drawIntoCanvas { nativeCanvas }` and a
  `StaticLayout` per overlay — **do not mutate the decoded bitmap** (the legacy
  Glide `asBitmap` + `Canvas(onBitmap)` approach).
- **Coordinate mapping (faithful to legacy):** x/y are the text center in % of the
  page; w is the constrained text width in % of the page width; `size` is in image
  pixels and is scaled by `pageWidth / intrinsicWidth` (the image's intrinsic ->
  displayed ratio, captured from Coil's `onSuccess`). This reproduces legacy
  `MangaHelper.drawText` exactly while staying density- and zoom-independent:
  - paint: black `#000000`, `ANTI_ALIAS`, `letterSpacing = 0.1`, `ALIGN_CENTER`;
  - `constrainedWidth = w/100 * pageWidth`;
  - `translate(x = x/100*pageWidth - layoutWidth/2, y = y/100*pageHeight - layoutHeight/2)`.
- **Performance:** the manga `Typeface` (`fonts/manga.ttf`) and the per-overlay
  `StaticLayout`s are built once and `remember`ed, keyed by
  `(overlays, pageWidth, pageHeight, intrinsicWidth)`. Pinch-zoom changes only the
  graphics matrix, not the page-box size, so layouts are **not** rebuilt during a
  gesture. The typeface falls back to `Typeface.DEFAULT` if the asset is missing —
  never crash the page on a font error.

---

## 6. Timing semantics

Mirror `MessageImageNodeHandler`: `Delay(seenMs.coerceAtLeast(520))` then emit the
message. Map authored fields as `delay → seenMs`, `duration → waitMs`.
`isAutoTiming`/`isHesitating` are parsed for authoring compatibility but, like
`message-image`, are not interpreted by the handler (no typing-bubble lead-in for
a page). This keeps control flow identical to the image path and avoids a second
timing policy to maintain.

---

## 7. Edge cases & invariants (validated at boundaries)

- Parser: `require` non-blank `assetFileName`; drop the node if `messages`
  resolves to empty; per-message defaults for missing numeric fields.
- Engine: handler is a no-op (`HandlerScript()`) if the node is not
  `Node.MangaPage` (defensive cast, consistent with other handlers).
- UI: `onMangaClick` default is a no-op, so forgetting to wire it compiles and
  fails soft.
- Rendering: missing font → `Typeface.DEFAULT`; zero-size/empty image → show
  nothing rather than throw.
- Coroutines: any `catch` in the page loader must rethrow
  `CancellationException` first (project rule).

---

## 8. Tests (smallest practical, run debug / no cache)

1. **Parser unit test** — feed the JSON in §1; assert a single `Node.MangaPage`
   whose `imageUrl` ends with `assets/fc81f14a-…e3.webp`, `messages.size == 1`,
   and the message fields equal `{size=28, x=74.3, y=30.4, w=22}` with `text`
   still containing the raw `[prenom]` token (substitution is the handler's job).
2. **Handler unit test** — memory `heroName = "Léa"`; assert the emitted
   `GameMessageMangaPage.messages[0].text ==
   "Au revoir Léa, sache que tu ne seras jamais seul"`.
3. **Mapper/UI smoke** — render `Message(...)` with a `GameMessageMangaPage` and
   verify `MessageManga` is composed and `onMangaClick` fires with the overlays.

Validate with: `./gradlew :game:data:testDebugUnitTest :game:domain:testDebugUnitTest --no-build-cache`
(adjust task names to the module test tasks).

---

## 9. Reference (legacy — inspiration only, do not copy)

`/Users/hb/Documents/Coding/Android/backup/smsgame - 2026:02:27/`
- `activities/manga/MangaPageActivity.kt` — Activity that loaded the bitmap and
  displayed the composited result (separate screen → replaced by our overlay).
- `activities/manga/MangaHelper.kt` — `drawText(...)` (percent coordinate math,
  font `fonts/manga.ttf`, `StaticLayout`, centered) and `parseMessage(...)`
  (legacy newline-encoded `text/size/x/y/w` → replaced by structured JSON).
- `activities/manga/MangaMessage.kt` — parcelable of `{text,size,x,y,w}`.
- `activities/smsgame/items/PhraseMangaPage.kt` — list item with a circle avatar
  and an **Open** button (→ `MessageManga`).

Legacy used a separate `Activity`, mutated the bitmap off Glide, and parsed a
fragile `key:value` newline format. We replace all three with: an in-screen
Compose overlay, non-destructive `Canvas` text drawing, and a typed JSON schema.

---

## 10. Roll-out checklist

- [ ] Domain: `Node.MangaPage`, `NodeType.MANGA_PAGE`, `GameMessageType.MangaPage`,
      `GameMessageMangaPage`, `MangaPageNodeHandler`, factory branch.
- [ ] Data: `NodeDataDto` + `MangaMessageDto`, `ChapterGraphParser` branch.
- [ ] Presentation: mapper branch + `onMangaClick`, `MangaPageScreen` overlay,
      `SmsGameScreen` state wiring.
- [ ] Font loader for `fonts/manga.ttf` with fallback.
- [ ] Tests in §8 green (debug, no cache).
- [ ] Visual QA vs one legacy page for text size/position parity.

**Awaiting approval to proceed with implementation.**

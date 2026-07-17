This file details the layout adaptation technology about GameCard composable.

Displaying a GameCard is a Challenge because the title of the Game is merge within the image and not
made via a Text composable-this is done so because the fonts are special to a Game.

So the challenge is to respect a positioning and displaying of areas (title, image, dead-area) for
many screen sizes.

Important details:

- The image aspect ratio is 2767px*1139px
- The Tile is in a rectangle at the following position:

> > Top left corner : 125px / 129px
> > Top Right corner : 1308px / 129px
> > Bottom right corner : 1308px / 747 px
> > Bottom left corner : 125px / 747 px

- The subtitle (themes) must be under that rectangle and centered horizontally.

Example of subtitle (accepts an array of strings themes) : "MYSTÈRE • ROMANCE • CORÉE" (where the
separators must be red and the themes white)

What do you recommend to our Design team and how can we code that later?

---

# Team answer (Design + Engineering)

## 1. Normalized geometry (single source of truth)

Everything must be expressed as **fractions of the image**, never in dp or absolute pixels, so the
layout is identical at any screen size:

| Element | Value (px) | Fraction of 2767×1139 |
|---|---|---|
| Title rect | x: 125→1308, y: 129→747 | left 4.5%, top 11.3%, right 47.3%, bottom 65.6% |
| Title center X | 716.5 | **25.9% of width** |
| Dead area (title-free) | x: 1308→2767 | right 52.7% of the image |
| Subtitle zone | y: 747→1139 | band height 34.4%, vertical center ≈ 82.8% |

Key insight: the subtitle must be centered on the **title rectangle** (25.9% of width), **not** on
the whole card. Centering it on the card would drift it right of the baked-in title.

## 2. Recommendations to the Design team (Don Norman / John Nevarez)

1. **Never crop the banner.** Enforce the 2767:1139 (≈ 2.43:1) ratio everywhere the card appears.
   `ContentScale.Crop` would cut the baked-in title at some screen widths — unacceptable since the
   title IS the image. Use `Fit` inside an aspect-ratio-locked container instead.
2. **Respect the dead area (x > 1308).** This right half is the only place for decorative art /
   characters. Nothing textual or interactive will ever live there; keep it visually quiet so the
   title remains the focal point.
3. **Keep the subtitle zone clean (y > 747).** No important artwork in that band: the themes text
   overlays it. Prefer a subtle darkening gradient behind the subtitle for legibility on bright art.
4. **Cap themes at 3 short uppercase labels** ("MYSTÈRE • ROMANCE • CORÉE"). More or longer labels
   will overflow the 1183px-wide title column on small phones. Themes are data, not design — deliver
   them per game, not baked into the image (they must be localizable and dynamic).
5. **Minimum legibility:** subtitle ≈ 18sp Poppins SemiBold on a 360dp-wide card; verify contrast
   (WCAG AA) of white-on-artwork, hence recommendation 3.

## 3. How to code it (Leland / Romain / Holzmann / Liskov)

**Approach: one aspect-ratio-locked `Box` + fractional positioning. No custom `Layout`, no
`SubcomposeLayout` — Holzmann: keep the control flow stupidly simple.**

```kotlin
// GameCardGeometry.kt — the ONLY place these numbers exist
const val GAME_CARD_ASPECT = 2767f / 1139f
const val TITLE_CENTER_X_FRACTION = 716.5f / 2767f   // 0.259
const val TITLE_BOTTOM_Y_FRACTION = 747f / 1139f     // 0.656
```

```kotlin
@Composable
fun GameCard(modifier: Modifier = Modifier, gameCatalog: GameCatalog, onTap: (GameCatalog) -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(GAME_CARD_ASPECT)          // replaces the fixed 140.dp height
            .clickable { onTap(gameCatalog) }
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = gameCatalog.bannerImageRequest(LocalContext.current),
            contentScale = ContentScale.Fit,        // never Crop: title is baked in
        )
        Themes(
            themes = gameCatalog.themes,            // data-driven, uppercase, max 3
            modifier = Modifier
                .align(Alignment.TopStart)
                .offsetFromTitleRect()              // see below
        )
    }
}
```

Positioning the subtitle under the title rect, centered on 25.9% of the width:

```kotlin
private fun Modifier.offsetFromTitleRect(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val x = (constraints.maxWidth * TITLE_CENTER_X_FRACTION - placeable.width / 2).roundToInt()
    val y = (constraints.maxHeight * TITLE_BOTTOM_Y_FRACTION).roundToInt() +
            8.dp.roundToPx()                          // small breathing gap under the rect
    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(x, y) }
}
```

**Themes composable contract (Liskov):** input `List<String>`; output one line, themes white,
separators red, ALL separators colored (not just the first):

```kotlin
themes.joinToString(separator = " • ")              // NOT joinToString { "•" }
// buildAnnotatedString: append theme in white, append " • " in Color.Red for each gap
```

## 4. Implementation status (`game_catalog/GameCard.kt`) — DONE

All previously identified gaps are fixed:

- ~~Fixed `140.dp` height~~ → `aspectRatio(2767/1139)` on the card.
- ~~`ContentScale.Crop`~~ → `ContentScale.Fit` (title can no longer be sliced).
- ~~Unaligned `Themes`~~ → `Modifier.underTitleRect()` places it under the title rect, centered on 25.9% of the width.
- ~~`joinToString { "•" }` transform bug~~ → annotated string with red " • " separators, all of them.
- ~~Pink separators / first-match-only highlight~~ → red separators, white uppercase themes.
- ~~Hardcoded themes + stray overlapping `Text`~~ → themes come from `GameCatalog.narrativeThemes`, trimmed, blanks dropped, capped at 3 (overflow logged in debug).
- **Adaptive fitting (long labels, e.g. German):** themes are measured at natural width as
  candidates (3 → 2 → 1); the first that fits on one line inside the card wins. If even a
  single theme is too wide, it is ellipsized (`maxLines = 1`, `TextOverflow.Ellipsis`), and
  each label is hard-capped at 24 chars at the boundary. The card has `clipToBounds()`, so the
  subtitle can never paint over neighbouring UI.
- ~~Magic numbers~~ → single source in `GameCardGeometry.kt`.
- Debug-only assertion in `underTitleRect()` fails loud if the card ever loses its aspect ratio (Holzmann).
- TalkBack: the card exposes `metadata.title` + themes as content description, since the title is an image.

## 5. Open action item — Design team

- [ ] **Legibility check at 320dp width with a real banner.** At that width the baked-in title
  is only ~137dp wide. If the game's custom font is not legible there, recommendation #1
  ("never crop") needs a fallback: minimum card height with crop of the dead area ONLY
  (x > 1308). Engineering is blocked on this verdict before adding any fallback logic.

## 6. Validation checklist (before merging)

- [ ] Card at 320dp, 360dp, 600dp widths: subtitle stays centered under the title rect.
- [ ] Title never cropped (Fit + locked aspect).
- [ ] 1, 2, and 3 themes all render correctly; separators colored, themes white; 4+ themes dropped.
- [ ] Long labels (e.g. German): subtitle degrades 3 → 2 → 1 themes, then ellipsizes; never leaks outside the card.
- [ ] Screenshot test (Paparazzi/Roborazzi) with a reference banner marking the rect at 125/129/1308/747.
- [x] TalkBack: card exposes game title + themes as content description (title is an image!).

This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.

## Task 1 [DONE] : Netflix-style vertical poster row on the home screen

Reference: "/Users/hb/Documents/Sutoko/Stories/New stories
/drive-download-20260801T022436Z-1-001/IMG_5983.PNG" (Netflix "Seulement sur Netflix" row:
portrait posters scrolling horizontally inside the vertically scrolling feed).

### User story

As a player browsing the home screen, I want some stories to appear as tall portrait posters
in a horizontally scrollable row (like Netflix), so that the feed feels richer and featured
stories get a premium showcase instead of a full-width banner.

### Specification

1. **Catalog field** — `GameCatalog` gains:
   - `cardLayout: CardLayout` (enum, default `CardLayout.HORIZONTAL`). Server key: `"cardLayout"`,
     values `"HORIZONTAL"` / `"VERTICAL"`. Unknown or missing values fall back to `HORIZONTAL`
     so older backends never break the feed.
   - `verticalBanner: Asset?` (server key: `"verticalBannerAsset"`) — the portrait poster image.
     Persisted in Room like `banner` (Gson JSON); `cardLayout` stored as TEXT. DB migration 18 → 19.
2. **UI** — new `GamePosterCard` composable in `game:presentation/game_catalog/`:
   portrait card (2:3 aspect, rounded corners) rendering `verticalBanner` via Coil,
   tap opens the game preview like any other card.
   New `GamePosterRow`: a smooth horizontal `LazyRow` of `GamePosterCard`s.
3. **Home screen** — stories with `cardLayout == VERTICAL` are pulled out of the classic
   full-width list and rendered as one `GamePosterRow` section (after the square stories,
   before the full-width cards). `HORIZONTAL` stories are untouched.
4. **Test data (debug only)** — fake VERTICAL stories are injected on the home screen in debug
   builds, using these posters bundled as debug assets:
   - Image 1: "/Users/hb/Documents/Sutoko/Stories/New stories /GANG/affiche.png"
   - Image 2: "/Users/hb/Documents/Sutoko/Stories/New stories /Dalsoon/Affiche.png"
   - Image 3: "/Users/hb/Documents/Sutoko/Stories/New stories /Close the Door/affiche.png"
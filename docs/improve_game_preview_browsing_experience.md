# GamePreview → Game browsing transition

## Goal
When the user taps **Play** on `GamePreview`, the app must dive them straight into the game's mood with a smooth, cinematic fade. There must be no sliding transition, no default system Activity animation, and no visual glitch.

## Spec
- **Fade out from `GamePreview` to black:** 500 ms.
- **Hold on black:** while the target game Activity is started (no system animation).
- **Fade in from black to the game:** 500 ms.
- **Total perceived transition:** ~1000 ms.
- **Reset on return:** when the user comes back to `GamePreview`, the black overlay is cleared immediately so the preview is visible again.

## Implementation

### Modern SMS games (`SmsGameActivity`)
1. `GamePreview.kt` animates a full-screen black overlay from `alpha = 0` to `1` over 500 ms when `GamePreviewEvent.PlayGame` is received. Only after the fade completes does it invoke `onNavigateToGame(...)`.
2. `MainActivity.kt` starts `SmsGameActivity` and calls `overridePendingTransition(0, 0)` to suppress the default slide.
3. `SmsGameActivity.kt` initializes its existing `SmsGameNavHost` overlay at `alpha = 1` and animates it to `0` over 500 ms after the first composition, revealing the game smoothly from black.
4. The separate 1000 ms fade inside `SmsGameScreen.kt` was removed so the two overlays do not fight.

### Legacy Friendzoned games (`friendzone1..4`)
1. `GamePreview.kt` and `MainActivity.kt` handle the source fade and the no-animation launch of the legacy `Load` Activity as described above.
2. Each Friendzoned `Load` Activity starts the real game/cinematic Activity with a shared cross-fade:
   - Entering game Activity fades in: `alpha 0 → 1` over 500 ms.
   - Exiting `Load` Activity fades out: `alpha 1 → 0` over 500 ms.
3. The animation XMLs live in `:shared-elements` so all four modules stay consistent:
   - `shared-elements/src/main/res/anim/game_launch_fade_in.xml`
   - `shared-elements/src/main/res/anim/game_launch_fade_out.xml`

## Files touched
- `game/presentation/src/main/java/com/purpletear/game/presentation/game_preview/GamePreview.kt`
- `app/src/main/java/fr/purpletear/sutoko/screens/MainActivity.kt`
- `game/presentation/src/main/java/com/purpletear/game/presentation/game_play/SmsGameActivity.kt`
- `game/presentation/src/main/java/com/purpletear/game/presentation/game_play/SmsGameScreen.kt`
- `games/friendzone1/src/main/java/fr/purpletear/friendzone/activities/load/Load.kt`
- `games/friendzone2/src/main/java/fr/purpletear/friendzone2/activities/load/Load.kt`
- `games/friendzone3/src/main/java/friendzone3/purpletear/fr/friendzon3/Load.java`
- `games/friendzone4/src/main/java/fr/purpletear/friendzone4/game/activities/load/Load.java`
- `shared-elements/src/main/res/anim/game_launch_fade_in.xml`
- `shared-elements/src/main/res/anim/game_launch_fade_out.xml`

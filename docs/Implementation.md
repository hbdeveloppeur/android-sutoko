This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.

## Problem 1 : GamePreview Background Image - Background Video weird switch.

If the Game has a Background Image and a Background video url:

- Step 1: The background Image is loaded (good)
- Step 2: The background image fades out (ugly choice)
  Unpleasant black screen.
- Step 3: The background video fades in ()
  Sachant que souvent la background Image est la première frame de la video

**FIXED** (branch `fix/game-preview-background-crossfade`):
The video now loads *under* the image, and the image only fades out once the
video's first frame is actually rendered (`MEDIA_INFO_VIDEO_RENDERING_START`).
Result: a true crossfade from image to video — no black gap. If the video fails
to start, the image simply stays on screen.
Changed: `game/presentation/.../game_preview/BackgroundMedia.kt` (added
`onFirstFrame` callback, removed internal black overlay fade) and
`GameBackgroundPreviewMedia.kt` (image fade-out now driven by first-frame event).
Validated with `:game:presentation:assembleDebug` and `testDebugUnitTest`
(debug, `--no-build-cache`).
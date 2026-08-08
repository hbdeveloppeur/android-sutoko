This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*

You are an expert in UX/UI design.

# Error in GamePreview: whatever we do sometimes clicking on "Download Preview" doesn't download it.

**Status: FIXED** (branch `fix/game-preview-download-retry`)

Root cause: any preview download failure (even transient: network timeout, 5xx, user token
not yet loaded) permanently hid the button for the screen's lifetime, with zero user feedback.
One flaky call made the feature look dead.

Fix: `GamePreviewViewModel.onPreviewDownloadFailure` now only hides the feature on a definitive
server 403 (`GameUiError.DownloadForbidden`, per backend contract). Transient failures keep the
button visible and show an error toast, so retrying is one tap away.
Validated: `:game:presentation:testDebugUnitTest` (102 tests, 0 failures, debug, no cache).
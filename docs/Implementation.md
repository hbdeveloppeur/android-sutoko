This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Task 1 : GamePreview - fixes to implement: [DONE]

1. Can you confirm users that if the current chapter is not available, users reach a weird state-the
   button "play" has hidden text?
   -> Confirmed: the Play button was disabled when the chapter was unavailable, which dimmed its
   text to alpha 0.2 ("hidden") and silently swallowed taps.
2. Users must be able to see it and when click play, a toast appears with the same text as
   GamePreviewUnavailable
   -> Done: the Play button stays fully visible once the chapter is loaded; tapping it shows a
   toast with the same "Next chapter - <date>" text as GamePreviewUnavailable (shared
   Chapter.formatReleaseDate helper). Admins still bypass and play. Unit tests added in
   GamePreviewViewModelTest; :game:presentation:testDebugUnitTest and :app:assembleDebug pass.
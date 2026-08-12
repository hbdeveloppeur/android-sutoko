This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*

You are an expert and smart UX/UI expert.
This operation is done because we need to save tokens for our AI models-the files are too long.

# Fix on the GameEngine download button: [DONE - branch fix/game-download-button-progress]

- [DONE] The button always displays "10 mo" - just replace it with "" (an empty string)
  - Removed the hardcoded size subtitle (`game_menu_download_size` strings deleted in all
    locales); the Download button now has no subtitle.
- [DONE] When downloading, it's unpleasant because it always displays "0%" no progress even with
  big archive downloads.
  - Root cause: `progress` is a 0f..1f fraction but was displayed with `progress.toInt()`.
    Now scaled to a percent: `(progress * 100).toInt()`.
  - Validated by `GameButtonsStateTest` (debug, --rerun-tasks): all green.
This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*
Do never delete implementation.md.

You are an expert and smart UX/UI expert.
This operation is done because we need to save tokens for our AI models-the files are too long.

# [Done] Fix: The feature to pause the game must not be enabled in Click to progress mode.
# Branch: fix/disable-pause-in-click-to-advance — hold-to-pause is ignored in CLICK_TO_ADVANCE
# mode (GameEngineViewModel.onHoldPauseChanged early-return + hold released on mode switch).
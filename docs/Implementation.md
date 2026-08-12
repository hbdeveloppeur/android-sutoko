This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*

You are an expert and smart UX/UI expert.

# [DONE] Review: When the option "Make a choice" appears in the game we are forced to click the MakeAChoiceButton - we want that a progress click opens the choice box (do not conflict with scroll)

Fixed in GameEngineViewModel.onAdvanceOnTap: a progress tap while isAwaitingInput now reveals
the choice box (same as MakeAChoiceButton). Scroll, button and overlay taps consume the gesture
before it reaches this handler, so no conflict.
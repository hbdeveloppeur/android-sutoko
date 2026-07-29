This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Task [DONE - branch fix/character-slider-fade-in]: You are an UX/UI designer - Go to Ai Friend tab,
the characters avatars composables in CharacterSlider must appear in fade in, it's not the case for
the moment and it creates a unpleasant effect.
-> Fixed: each avatar in CharacterSlider now fades in (300ms, 50ms stagger per item) via an
Animatable alpha. Debug build :ai-conversation:presentation:assembleDebug --no-build-cache passed.
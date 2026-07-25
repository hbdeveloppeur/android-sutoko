This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Task 1 : Fix the video in Ai Home Screen.
Behavior : Opening the screen, the video starts, then putting app in background, then foreground,
the video goes black and the logs spams "Out of order buffers detected for
RequestedLayerState{fr.purpletear.sutoko/fr.purpletear.sutoko.screens.MainActivity#25467
parentId=25466} producedId=2 frameNumber=4982 -> producedId=2 frameNumber=1168" many may times.
Fix that
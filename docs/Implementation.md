This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.

Task 1: A Story/Game has a new field in endpoints, it's "menuSoundUrl" (nullable). Play it only in
GamePreview with the following rules:

- Appears in fade in slowly (1 sec), Disappears in fades out (1 sec)
- Add a clean icon to disable to sound in top of the GamePreview screen next to share
- Save the state in memory for future use even if the app is closed
- Clean integration, clean code, memory and states management
- Make the volume not too loud for it (70%). It has to play in loop-between each loop there is a 5
  seconds wait.
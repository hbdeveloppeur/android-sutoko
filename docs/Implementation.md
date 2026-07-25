This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is not main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Human testers feedback : This is the test of starting the app without connectivity (Air plane mode)
Feedback :

- Opening shop displays -1 coins and -1 diamonds. It's better to have a button to log in. Same in
  Account screen.
- In Compagnon / Sutoko Ai Home Screen pressing "Start the conversation" does nothing, at least a
  toast to say there is no connectivity would be good
- GamePreview displays a Toast "Unable to load story" It's not clear enough, right?
- GamePreview - Clicking on Download game makes a crash with the following stack trace : """
  2026-07-25 01:36:24.096 23250-23250 AndroidRuntime fr.purpletear.sutoko E FATAL EXCEPTION: main
  Process: fr.purpletear.sutoko, PID: 23250
  2026-07-25 01:36:24.141 23250-23255 rpletear.sutoko fr.purpletear.sutoko I Background young
  concurrent mark compact GC freed 10032KB AllocSpace bytes, 25(1792KB) LOS objects, 46% free,
  12MB/23MB, paused 244us,8.444ms total 50.796ms
  2026-07-25 01:36:24.244 23250-23300 FirebaseCrashlytics fr.purpletear.sutoko W Unable to read App
  Quality Sessions session id.
  2026-07-25 01:36:24.332 23250-23250 Process fr.purpletear.sutoko I Quit itself, Pid:23250
  StackTrace:
  com.android.internal.os.RuntimeInit$KillApplicationHandler.uncaughtException:201 com.google.firebase.crashlytics.internal.common.CrashlyticsUncaughtExceptionHandler.uncaughtException:63 java.lang.ThreadGroup.uncaughtException:1098 java.lang.ThreadGroup.uncaughtException:1093 com.android.internal.os.RuntimeInitExtImpl.uncaughtExceptionExt:72 com.android.internal.os.RuntimeInit$
  LoggingHandler.uncaughtException:138
  com.android.internal.os.RuntimeInit$KillApplicationHandler.ensureLogging:226 com.android.internal.os.RuntimeInit$
  KillApplicationHandler.uncaughtException:171
  com.google.firebase.crashlytics.internal.common.CrashlyticsUncaughtExceptionHandler.uncaughtException:
  63 java.lang.ThreadGroup.uncaughtException:1098
  2026-07-25 01:36:24.332 23250-23250 Process fr.purpletear.sutoko I Sending signal. PID: 23250 SIG:
  9
  ---------------------------- PROCESS ENDED (23250) for package
  fr.purpletear.sutoko ----------------------------"""""""
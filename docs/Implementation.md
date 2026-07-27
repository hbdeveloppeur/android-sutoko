This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Problem 1: After branch new uninstall/install - the game story with id 5JZvpvXaS6r has all chapters
available but it still displays a message "Prochain chapitre - mercredi 1 janvier"

- These chapters are all available since 2020, you can check with curl "
  GET https://sutoko.com/api/story/5JZvpvXaS6r/chapters"

[DONE] Root cause: Chapter.toEntity() (game/data/.../local/entity/ChapterEntity.kt) dropped the
`available` flag, so every chapter persisted to Room was stored as unavailable after a fresh
install. The date shown (Jan 1st) was the chapter's real 2020 release date, formatted without
the year. Fix: persist `available` in toEntity(); the next getChapters() fetch self-heals
existing installs (REPLACE upsert). Regression test: ChapterEntityMapperTest.
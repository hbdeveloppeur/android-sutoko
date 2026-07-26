This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Task 1 : The friendzoned games (available as modules in :games:friendzone*) are managing themselves
the current chapter progress. This creates a new problem because GamePreview-originally created
for :game module-also lead to friendzoned games.
We chose that because we didn't want to develop a GamePreview screen for each game.

1. We are sure that we don't want users to be able to switch chapters from GamePreview when it is a
   Friendzoned game.
2. How, without modifying :games:friendzone* code bases to see the right chapter number and to be
   able to restart the game via the restart button?

What is the cleanest way to do? Propose me a plan before coding.

-> Done (branch fix/friendzone-game-preview-progress), without touching :games:friendzone*:
   - New domain contract FriendzonedProgressRepository (+ FriendzonedLegacyIds: 159, 161, 162,
     163 - SMS 160 excluded, it runs on the standard engine) backed by TableOfSymbols (:tools),
     the store the friendzone games read/write themselves.
   - ChapterRepositoryImpl.observeCurrentChapter now branches on the game's legacyId: friendzoned
     games resolve their current chapter from TableOfSymbols (exact code, then same chapter
     number, then first chapter as fallbacks); every other game keeps the Room user-progress path
     unchanged. GamePreview re-reads on ON_RESUME, so the chapter number is fresh after a session.
   - Chapter switching is hidden for friendzoned games: the Chapters button in GamePreview and the
     chapter-code section in the tester options screen.
   - RestartGameUseCase takes an optional legacyId and also resets the TableOfSymbols row (same
     reset semantics the games use themselves: story version preserved) for friendzoned games.
     After a confirmed restart the preview re-reads the current chapter immediately (tester
     feedback: the Play button kept showing the pre-restart chapter until the next ON_RESUME).
   - Unit tests: RestartGameUseCaseTest (domain), ChapterRepositoryImplFriendzonedTest (data),
     friendzoned restart test in GamePreviewViewModelTest. :game:* tests pass and
     :app:assembleDebug succeeds (debug, no cache). The 3 ChapterGraphParserTest failures are
     pre-existing on main (verified in a clean worktree).
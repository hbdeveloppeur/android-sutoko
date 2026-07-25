This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Task 1 : In the Home Screen, GameCard composable: if a Game has one or more new chapters soon, the
categories are replaced
with a text "New chapters soon" whitened green text.

✅ Done (branch feature/chapters-screen): GameCard has a new `hasNewChaptersSoon` flag that swaps
the theme labels for an uppercase "New chapters soon" label in whitened green (0xFF90EE90),
same position/fitting as the themes, TalkBack included. Driven by
`ChapterRepository.observeStoryIdsWithUpcomingChapters()` (Room: chapters with a future
releaseDate); `HomeScreenViewModel` exposes `newChaptersSoonGameIds` and warms the chapters
cache once per game per session so the badge reflects server data. Localized en/fr/de/es.
Validated: debug compile (no cache) green; :game:presentation + :game:domain unit tests green.
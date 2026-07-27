This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Task 1 [DONE - branch fix/chapter-language-fallback] : When a game starts it loads the chapter at "
/data/user/0/fr.purpletear.sutoko/files/games/180/chapters/fr-ES/1a"
But there is a problem here, "fr-ES" is never in the list of avalaible languages.
The only possible languages are fr-FR, es-ES, es-419, de-DE, en-GB, en-US
So we want the closest language: for instance fr-ES would use fr-FR, es-MX would use es-ES, etc.

Fix: ChapterGraphRepositoryImpl now resolves the requested locale tag against the
language directories actually present on disk via ChapterLanguageResolver
(exact match -> same language canonical dir -> en-US/en-GB fallback).
Covered by ChapterLanguageResolverTest (7 tests).
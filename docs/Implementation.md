This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.

## Problem 1 : In the section "My published stories" stories cannot be opened when online - even when online when you click open, an error message saying something like "story not online yet"

**FIXED** (branch `fix/published-stories-open-online`)
Root cause: the app checked `GameDto.status == "published"`, but the backend returns
`status: "online"` for published stories (verified against https://sutoko.com/portal/stories/*).
`isOnline` was therefore always false and the open button always showed the
"story not online yet" toast.
Fix: `GameRepositoryImpl.getOneUserGames` now checks `status == "online"` (null-safe).
Unit tests updated/added in `GameRepositoryImplSearchStoriesTest` (all pass, debug, no cache).

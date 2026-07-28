This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Problem 1: Analyse the conversation between the Android dev and Backend api dev, analyze and propose
a plan. Deep analyze and ask your team for a plan.

Android dev: "Hey mate, I saw that my users never receive a link when trying to download a paying
game when they are Premium"
Backend dev: "Hey. Alright. I checked, everything is good by my side, can you give me an example of
premium user."
Android dev: "8be954c7a18f4e7cba9c is a good example"
Backend dev: "After checking, in the database is not premium. Is premium any users that have
acknowledged a sku containing the word 'premium' in it."
Android dev: "I see... I will have to check by my side if when the app starts, the premium sku is
registered."

[DONE] Root cause: a two-sided split-brain around premium entitlement.

1. The premium SKU (e.g. `premium_month_9_49`, a subscription) was claimed by NO
   `PurchaseBackendRegistrar`: `StoryPurchaseBackendRegistrar` only matched `^story_\d+$` and
   `AiMessagePackPurchaseBackendRegistrar` only message packs. Worse,
   `PurchaseBackendRegistrationCoordinator` silently called `markBackendRegistered()` for any
   unclaimed SKU. So the premium purchase was NEVER sent to `order/register`, the backend never
   acknowledged a SKU containing "premium" for the user -> user not premium in the DB -> no
   download link. Exactly user 8be954c7a18f4e7cba9c's case.
2. The app showed the user as premium anyway: `observeHasGlobalPremium()` only checked the local
   Room row (PURCHASED + sku contains "premium"), ignoring `backendRegistered`. App said premium,
   server disagreed.

Fix (branch fix/entitlement-single-source-of-truth):
- `StoryPurchaseBackendRegistrar.supports()` now also claims SKUs containing "premium"
  (case-insensitive), so premium purchases are registered via `order/register` at app start
  through the existing retry/backoff pipe.
- Room migration 3->4 resets `backendRegistered = 0` for premium SKUs, so existing installs
  (silently flagged in the past) re-register once at next app start.
- New `EntitlementRepository` (shop) is the single source of truth for story access, fail-closed:
  granted only when server-confirmed (billing purchase with `backendRegistered == true`, coin
  grant, or server-registered premium). GamePreview/Create/Account ViewModels now consume it
  instead of raw local purchase state.
- Backend definitive rejection (HTTP 4xx) throws `PurchaseRegistrationRejectedException`; the
  coordinator purges the local purchase instead of retrying forever.
- Coin-grant cache (`InMemoryCoinPurchaseRepository`) is now scoped per user id, so a grant is
  never leaked across accounts on the same device.
- The coordinator now logs a warning when no registrar claims a SKU (fail-visible).

Regression tests: StoryPurchaseBackendRegistrarTest (supports/registers premium),
PurchaseBackendRegistrationCoordinatorTest (rejection purge, retry), EntitlementRepositoryImplTest,
GamePreviewViewModelTest, InMemoryCoinPurchaseRepositoryTest.

This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is not main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Task : Some users bought games but don't see them as granted, fix this.
I found several real scenarios. The healing mechanism (syncCoinPurchaseGrantOnDataLoad) is one-shot
with no retry, so anything that goes wrong at that single moment leaves
the story displayed as "not bought" even though the user is connected and did buy it:

For coin purchases (most exposed):

1. Transient remote failure, no retry. The one-shot userHasProduct call fails (timeout, server 500,
   flaky network) → onFailure just logs. coinGrantCheckDone was already set to tr
   before the call (line 424), so it never retries — not even on pull-to-refresh (refresh() only
   resets recoveryAttempted, not coinGrantCheckDone). The story shows "not bought" u
   l the screen/ViewModel is recreated.
2. Connect-after-load race. If the game data emits while isUserConnected is still false (auth still
   restoring), the check is skipped — fine — but the game flow doesn't include co
   ction state in its combine, so connecting afterwards doesn't retrigger the check. Unless some DB
   row changes to force a re-emission, the remote check never runs for that scree
   ession.

3. Connected-but-user-not-loaded race. IsStoryGrantedUseCase does userRepository.observeUser()
   .firstOrNull() ?: return success(false). If isUserConnected is already true but the
   r profile hasn't emitted yet, it returns false, and the ViewModel has already burned its one
   attempt → permanently "not bought" for this screen instance.

5. Room is only refreshed by PurchaseSyncCoordinator (app foreground / billing reconnect / purchase
   updates). If syncPurchases() fails at those moments, the preview shows "not bo
   t" — but this one retries on the next foreground/reconnect, so it's transient. One sharper edge:
   syncPurchases calls purchaseDao.replaceAll(receipts), so if the device's Play
   ount changed, previously synced purchases are wiped → "not bought" (arguably correct per Play
   semantics).
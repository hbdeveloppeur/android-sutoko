This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.

Task 1 (DONE): See the following stack trace, what do you recommend? What is the user connect while it's
trying, will it spam requests?

Resolution: no network spam (failure happened before any HTTP call), but NotConnectedException is a
deterministic precondition failure — retrying it 5x was pointless, and after giving up nothing
re-triggered registration when the user connected later (paid purchase never credited). Fix: the three
backend registrars (Story, CoinsPack, AiMessagePack) now suspend on observeUser().filterNotNull().first()
until a session exists instead of failing fast.

2026-07-30 23:14:44.692 26646-27232 BackendRegistration fr.purpletear.sutoko E Giving up backend
registration for story_159 after 5 attempts
2026-07-30 23:14:44.695 26646-27232 BackendRegistration fr.purpletear.sutoko W Backend registration
failed for story_163 (attempt 1/5) (Fix with AI)
com.purpletear.sutoko.domain.exception.NotConnectedException: User is not connected
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar.register-BWLJW6A(
StoryPurchaseBackendRegistrar.kt:38)
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar$register$1.invokeSuspend(
StoryPurchaseBackendRegistrar.kt:15)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.internal.ScopeCoroutine.afterResume(Scopes.kt:28)
at kotlinx.coroutines.AbstractCoroutine.resumeWith(AbstractCoroutine.kt:100)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:46)
at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:101)
at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:113)
at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)
at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:589)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:823)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:720)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:707)
2026-07-30 23:14:46.701 26646-27232 BackendRegistration fr.purpletear.sutoko W Backend registration
failed for story_163 (attempt 2/5) (Fix with AI)
com.purpletear.sutoko.domain.exception.NotConnectedException: User is not connected
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar.register-BWLJW6A(
StoryPurchaseBackendRegistrar.kt:38)
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar$register$1.invokeSuspend(
StoryPurchaseBackendRegistrar.kt:15)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:99)
at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:113)
at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)
at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:589)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:823)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:720)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:707)
2026-07-30 23:14:50.707 26646-26875 BackendRegistration fr.purpletear.sutoko W Backend registration
failed for story_163 (attempt 3/5) (Fix with AI)
com.purpletear.sutoko.domain.exception.NotConnectedException: User is not connected
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar.register-BWLJW6A(
StoryPurchaseBackendRegistrar.kt:38)
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar$register$1.invokeSuspend(
StoryPurchaseBackendRegistrar.kt:15)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:99)
at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:113)
at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)
at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:589)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:823)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:720)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:707)
2026-07-30 23:14:58.721 26646-27232 BackendRegistration fr.purpletear.sutoko W Backend registration
failed for story_163 (attempt 4/5) (Fix with AI)
com.purpletear.sutoko.domain.exception.NotConnectedException: User is not connected
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar.register-BWLJW6A(
StoryPurchaseBackendRegistrar.kt:38)
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar$register$1.invokeSuspend(
StoryPurchaseBackendRegistrar.kt:15)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:99)
at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:113)
at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)
at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:589)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:823)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:720)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:707)
2026-07-30 23:15:11.143 26646-26654 rpletear.sutoko fr.purpletear.sutoko I Background young
concurrent mark compact GC freed 13MB AllocSpace bytes, 0(0B) LOS objects, 50% free, 13MB/26MB,
paused 387us,9.474ms total 52.013ms
2026-07-30 23:15:14.729 26646-27232 BackendRegistration fr.purpletear.sutoko W Backend registration
failed for story_163 (attempt 5/5) (Fix with AI)
com.purpletear.sutoko.domain.exception.NotConnectedException: User is not connected
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar.register-BWLJW6A(
StoryPurchaseBackendRegistrar.kt:38)
at com.purpletear.sutoko.shop.data.registrar.StoryPurchaseBackendRegistrar$register$1.invokeSuspend(
StoryPurchaseBackendRegistrar.kt:15)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:99)
at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:113)
at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)
at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:589)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:823)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:720)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:707)
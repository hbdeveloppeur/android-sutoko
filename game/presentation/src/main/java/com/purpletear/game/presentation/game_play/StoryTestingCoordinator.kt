package com.purpletear.game.presentation.game_play

import android.os.Build
import com.purpletear.game.data.file.testing.TestAssetCacheManager
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.testing.TestEvent
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import com.purpletear.sutoko.game.usecase.testing.ApplyTestPackageUseCase
import com.purpletear.sutoko.game.usecase.testing.DownloadTestPackageUseCase
import com.purpletear.sutoko.game.usecase.testing.JoinTestSessionUseCase
import com.purpletear.sutoko.game.usecase.testing.LoadTestChapterGraphUseCase
import com.purpletear.sutoko.game.usecase.testing.ObserveTestEventsUseCase
import com.purpletear.sutoko.game.usecase.testing.RegisterAssetInventoryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryTestingCoordinator @Inject constructor(
    private val joinTestSession: JoinTestSessionUseCase,
    private val registerAssetInventory: RegisterAssetInventoryUseCase,
    private val observeTestEvents: ObserveTestEventsUseCase,
    private val downloadTestPackage: DownloadTestPackageUseCase,
    private val applyTestPackage: ApplyTestPackageUseCase,
    private val loadTestChapterGraph: LoadTestChapterGraphUseCase,
    private val assetCacheManager: TestAssetCacheManager,
    private val gameMemory: GameMemory,
) {

    private val _state = MutableStateFlow(StoryTestingState())
    val state: StateFlow<StoryTestingState> = _state.asStateFlow()

    private var coordinatorScope: CoroutineScope? = null
    private var currentGameId: String? = null
    private var currentStoryId: String? = null
    private var sessionId: String? = null
    private var inventoryToken: String? = null

    private val localSeeds = mutableMapOf<String, Int>()
    private val activeDownloads = mutableMapOf<String, Job>()

    /**
     * Starts a test session for [gameId] / [storyId].
     * Safe to call again; previous sessions are stopped first.
     */
    fun startTesting(gameId: String, storyId: String) {
        if (currentGameId == gameId && currentStoryId == storyId && _state.value.isActive) {
            StoryTestingLogger.d("SESS") { "startTesting ignored — already active for $gameId / $storyId" }
            return
        }

        stopTesting()
        currentGameId = gameId
        currentStoryId = storyId

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        coordinatorScope = scope

        _state.value = StoryTestingState(
            isActive = true,
            isLoading = true,
            connectionState = StoryTestingConnectionState.CONNECTING,
            error = null
        )
        StoryTestingLogger.i("SESS") { "Starting test session — gameId=$gameId, storyId=$storyId" }

        scope.launch {
            runCatching {
                joinAndSync(gameId, storyId)
            }.onFailure { error ->
                StoryTestingLogger.e("SESS", error) { "Failed to join test session" }
                _state.value = _state.value.copy(
                    isLoading = false,
                    connectionState = StoryTestingConnectionState.DISCONNECTED,
                    error = "Failed to join test session: ${error.message}"
                )
            }
        }
    }

    /**
     * Stops the active test session and cleans up resources.
     */
    fun stopTesting() {
        StoryTestingLogger.i("SESS") { "Stopping test session" }
        activeDownloads.values.forEach { it.cancel() }
        activeDownloads.clear()
        observeTestEvents.stop()
        coordinatorScope?.cancel()
        coordinatorScope = null

        gameMemory.setNamespace(null)

        sessionId = null
        inventoryToken = null
        localSeeds.clear()
        currentGameId = null
        currentStoryId = null

        _state.value = StoryTestingState()
    }

    private suspend fun joinAndSync(gameId: String, storyId: String) {
        val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} - Android ${Build.VERSION.RELEASE}"
        StoryTestingLogger.d("SESS") { "Joining session — storyId=$storyId, device=$deviceInfo" }

        val session = joinTestSession(storyId, deviceInfo).getOrThrow()
        sessionId = session.sessionId
        StoryTestingLogger.i("SESS") { "Joined session ${session.sessionId} — seeds=${session.chapterSeeds}" }

        val availableAssets = assetCacheManager.listAvailableAssets(gameId)
        inventoryToken = registerAssetInventory(session.sessionId, availableAssets).getOrThrow()
        StoryTestingLogger.d("SYNC") { "Registered inventory — ${availableAssets.size} available assets" }

        gameMemory.setNamespace("test-session-${session.sessionId}")
        StoryTestingLogger.d("MEM") { "Memory namespace set to test-session-${session.sessionId}" }

        observeEvents(session.sessionId)
    }

    private fun observeEvents(sessionId: String) {
        val scope = coordinatorScope ?: return
        StoryTestingLogger.d("NET") { "Observing SSE events for session $sessionId" }

        scope.launch {
            observeTestEvents(sessionId, inventoryToken)
                .catch { error ->
                    StoryTestingLogger.e("NET", error) { "SSE error" }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        connectionState = StoryTestingConnectionState.DISCONNECTED,
                        error = "SSE error: ${error.message}"
                    )
                }
                .collect { event ->
                    handleEvent(event)
                }
        }
    }

    private fun handleEvent(event: TestEvent) {
        StoryTestingLogger.d("NET") { "Received ${event::class.simpleName}" }
        when (event) {
            is TestEvent.Connected -> handleConnected(event)
            is TestEvent.PhoneConnected,
            is TestEvent.PhoneDisconnected -> { /* diagnostic / not emitted in v1 */
            }

            is TestEvent.SeedUpdated -> handleSeedUpdated(event)
            is TestEvent.PlayFromNode -> handlePlayFromNode(event)
            is TestEvent.Error -> {
                StoryTestingLogger.e("ERR") { "Server error [${event.code}]: ${event.message}" }
                val isTerminal = event.code == "connection_error" || event.code == "setup_error"
                _state.value = _state.value.copy(
                    isLoading = false,
                    connectionState = if (isTerminal) {
                        StoryTestingConnectionState.DISCONNECTED
                    } else {
                        _state.value.connectionState
                    },
                    error = "Server error [${event.code}]: ${event.message}"
                )
            }
        }
    }

    private fun handleConnected(event: TestEvent.Connected) {
        sessionId = event.sessionId
        val gameId = currentGameId ?: return
        StoryTestingLogger.i("SYNC") { "Connected — session ${event.sessionId}, seeds=${event.chapterSeeds}" }

        event.chapterSeeds.forEach { (chapterId, seed) ->
            val local = localSeed(chapterId)
            if (seed > local) {
                StoryTestingLogger.d("SYNC") { "Seed ahead — $chapterId: local=$local, server=$seed" }
                downloadAndApplyPackage(gameId, chapterId, seed, packageUrlFor(chapterId, seed))
            } else {
                StoryTestingLogger.d("SYNC") { "Seed up-to-date — $chapterId: local=$local, server=$seed" }
            }
        }

        _state.value = _state.value.copy(
            connectionState = StoryTestingConnectionState.CONNECTED,
            error = null
        )
        updateLoadingState()
    }

    private fun handleSeedUpdated(event: TestEvent.SeedUpdated) {
        val gameId = currentGameId ?: return
        val chapterId = event.chapterId
        val seed = event.seed
        val local = localSeed(chapterId)

        if (seed <= local || activeDownloads.containsKey(chapterId)) {
            StoryTestingLogger.d("SYNC") {
                "Seed update ignored — $chapterId: local=$local, server=$seed, downloading=${
                    activeDownloads.containsKey(
                        chapterId
                    )
                }"
            }
            return
        }

        StoryTestingLogger.i("SYNC") { "Seed updated — $chapterId: $local → $seed" }
        downloadAndApplyPackage(gameId, chapterId, seed, packageUrl = event.packageUrl)
    }

    private fun handlePlayFromNode(event: TestEvent.PlayFromNode) {
        val gameId = currentGameId ?: return
        val chapterId = event.chapterId
        val seedAtRequest = event.seedAtRequest
        val nodeId = event.nodeId
        val local = localSeed(chapterId)

        StoryTestingLogger.d("NAV") { "Play from node — chapter=$chapterId, node=$nodeId, seedAtRequest=$seedAtRequest, local=$local" }

        if (seedAtRequest < local) {
            StoryTestingLogger.w("NAV") { "Stale play request ignored — $chapterId seed $seedAtRequest < local $local" }
            return
        }

        _state.value = _state.value.copy(
            currentChapterId = chapterId,
            pendingNodeId = nodeId,
            targetNodeId = null
        )

        if (seedAtRequest > local) {
            val packageUrl = packageUrlFor(chapterId, seedAtRequest)
            StoryTestingLogger.d("NAV") { "Downloading newer package before playing — $chapterId seed $seedAtRequest" }
            downloadAndApplyPackage(gameId, chapterId, seedAtRequest, packageUrl = packageUrl)
            return
        }

        val graph = _state.value.currentGraph
        if (graph != null && graph.chapterCode == chapterId) {
            StoryTestingLogger.i("NAV") { "Playing from node $nodeId in $chapterId" }
            _state.value = _state.value.copy(
                targetNodeId = nodeId,
                pendingNodeId = null,
                playRequestCount = _state.value.playRequestCount + 1
            )
        } else {
            StoryTestingLogger.w("NAV") { "Graph not ready for $chapterId — node $nodeId queued" }
        }
    }

    private fun downloadAndApplyPackage(
        gameId: String,
        chapterId: String,
        seed: Int,
        packageUrl: String,
    ) {
        activeDownloads[chapterId]?.cancel()

        val scope = coordinatorScope ?: return
        _state.value = _state.value.copy(isLoading = true)
        StoryTestingLogger.i("PKG") { "Downloading package — $chapterId seed $seed" }

        activeDownloads[chapterId] = scope.launch {
            try {
                runCatching {
                    val extractedDir =
                        downloadTestPackage(packageUrl, gameId, chapterId, seed).getOrThrow()
                    applyTestPackage(extractedDir, gameId).getOrThrow()
                    localSeeds[chapterId] = seed
                    extractedDir
                }.onSuccess { extractedDir ->
                    StoryTestingLogger.i("PKG") { "Package applied — $chapterId seed $seed" }
                    onPackageApplied(extractedDir, gameId, chapterId)
                }.onFailure { error ->
                    StoryTestingLogger.e("PKG", error) { "Package failed — $chapterId seed $seed" }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Package download failed: ${error.message}"
                    )
                }
            } finally {
                activeDownloads.remove(chapterId)
                updateLoadingState()
            }
        }
    }

    private fun onPackageApplied(extractedDir: String, gameId: String, chapterId: String) {
        loadTestChapterGraph(extractedDir, gameId)
            .onSuccess { graph ->
                val pendingNodeId = _state.value.pendingNodeId
                val shouldPlayFromPending =
                    pendingNodeId != null && _state.value.currentChapterId == chapterId
                StoryTestingLogger.i("GRPH") { "Test graph loaded — ${graph.chapterCode}, ${graph.nodes.size} nodes, ${graph.edges.size} edges" }

                // If the author has not sent a PLAY_FROM_NODE event, start from the chapter's
                // first node so the "Try" button immediately launches the test session.
                val targetNodeId = when {
                    shouldPlayFromPending -> pendingNodeId
                    _state.value.targetNodeId == null -> graph.startNodeId
                    else -> _state.value.targetNodeId
                }

                // Increment the request counter whenever we issue a new play command,
                // including repeated "Play from this node" requests for the same node.
                val shouldIncrementPlayCount = targetNodeId != null &&
                    (shouldPlayFromPending || _state.value.targetNodeId == null)

                _state.value = _state.value.copy(
                    currentGraph = graph,
                    currentChapterId = chapterId,
                    targetNodeId = targetNodeId,
                    pendingNodeId = if (shouldPlayFromPending) null else _state.value.pendingNodeId,
                    playRequestCount = if (shouldIncrementPlayCount) {
                        _state.value.playRequestCount + 1
                    } else {
                        _state.value.playRequestCount
                    }
                )
            }
            .onFailure { error ->
                StoryTestingLogger.e("GRPH", error) { "Failed to load test graph" }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load test graph: ${error.message}"
                )
            }

        refreshInventoryToken(gameId)
    }

    private fun refreshInventoryToken(gameId: String) {
        val scope = coordinatorScope ?: return
        val sessionId = sessionId ?: return

        scope.launch {
            val availableAssets = assetCacheManager.listAvailableAssets(gameId)
            StoryTestingLogger.d("SYNC") { "Refreshing inventory token — ${availableAssets.size} assets" }
            registerAssetInventory(sessionId, availableAssets)
                .onSuccess { token ->
                    inventoryToken = token
                    StoryTestingLogger.d("SYNC") { "Inventory token refreshed" }
                }
                .onFailure { error ->
                    StoryTestingLogger.e("SYNC", error) { "Inventory token refresh failed" }
                }
        }
    }

    private fun updateLoadingState() {
        _state.value = _state.value.copy(isLoading = activeDownloads.isNotEmpty())
    }

    private fun packageUrlFor(chapterId: String, seed: Int): String {
        return "/test-package/$chapterId/$seed.zip"
    }

    private fun localSeed(chapterId: String): Int {
        return localSeeds[chapterId] ?: 0
    }
}

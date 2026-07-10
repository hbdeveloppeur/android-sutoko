package com.purpletear.game.presentation.game_play

import com.purpletear.sutoko.game.model.chapter.ChapterGraph

/**
 * Outcome of [decideCinematicResume]: what the game should do once a cinematic finishes.
 */
internal sealed class CinematicResumeAction {
    /** A story update arrived during the interlude; switch to it and resume there. */
    data class ApplyPendingGraph(
        val graph: ChapterGraph,
        val safeResumeNodeId: String?,
    ) : CinematicResumeAction()

    /** No update pending; resume the graph that was paused at [nodeId]. */
    data class ResumeOldGraph(val nodeId: String) : CinematicResumeAction()

    /** Nothing to resume (e.g. the cinematic was the end of the chapter). */
    data object None : CinematicResumeAction()
}

/**
 * Pure decision for what to do when a cinematic ends.
 *
 * If a story update arrived during the interlude ([pendingGraph] != null), switch to it and
 * re-resolve [resumeNodeId] against the new graph — falling back to the chapter start when that
 * node no longer exists. Otherwise resume the graph that was paused, at [resumeNodeId].
 *
 * Kept pure so the deferred-update policy is trivially unit-testable without a ViewModel harness.
 */
internal fun decideCinematicResume(
    resumeNodeId: String?,
    pendingGraph: ChapterGraph?,
): CinematicResumeAction = when {
    pendingGraph != null -> CinematicResumeAction.ApplyPendingGraph(
        graph = pendingGraph,
        safeResumeNodeId = resumeNodeId?.takeIf { pendingGraph.getNode(it) != null },
    )

    resumeNodeId != null -> CinematicResumeAction.ResumeOldGraph(resumeNodeId)

    else -> CinematicResumeAction.None
}

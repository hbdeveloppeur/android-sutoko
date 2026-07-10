package com.purpletear.sutoko.game.model.chapter

/**
 * Extracts the linear, non-interactive body of a cinematic: the nodes strictly between a
 * StartingCinematicNode (`[intro=start]`) and its matching EndingCinematicNode (`[intro=end]`).
 * Both markers are excluded from the result.
 *
 * The walk is intentionally a single bounded loop over raw graph edges — not `NodeResolver`, which
 * applies SMS choice-collapsing logic that is wrong for pure graph extraction. A cinematic is
 * linear by contract: any branch, cycle, dead end or unsupported node is an authoring error and is
 * reported as a value via [CinematicError].
 */
fun extractCinematicBody(
    graph: ChapterGraph,
    startNodeId: String,
    endNodeId: String,
): Result<List<Node>> {
    val body = ArrayList<Node>()
    val visited = HashSet<String>()

    var current: String? = graph.singleSuccessor(startNodeId)
        ?: return Result.failure(CinematicError.NonLinear(startNodeId))

    while (current != null && current != endNodeId) {
        if (!visited.add(current)) return Result.failure(CinematicError.Cycle(current))

        val node = graph.getNode(current)
            ?: return Result.failure(CinematicError.MissingNode(current))
        if (!node.isCinematicPlayable) {
            return Result.failure(CinematicError.UnsupportedNode(current, node::class.simpleName))
        }
        body.add(node)

        val nodeId = current
        current = graph.singleSuccessor(nodeId)
            ?: return Result.failure(CinematicError.NonLinear(nodeId))
    }

    return if (current == endNodeId) {
        Result.success(body)
    } else {
        Result.failure(CinematicError.EndNotReached(endNodeId))
    }
}

/** Nodes that `CinematicScreen` knows how to render. */
private val Node.isCinematicPlayable: Boolean
    get() = this is Node.Scene || this is Node.Sound || this is Node.IntroSentence

/** Value-based extraction errors. They are carried inside [Result.failure], never thrown. */
sealed class CinematicError(message: String) : IllegalArgumentException(message) {
    class NonLinear(val nodeId: String) :
        CinematicError("Cinematic is not linear at node '$nodeId' (expected exactly one successor)")

    class Cycle(val nodeId: String) :
        CinematicError("Cinematic contains a cycle at node '$nodeId'")

    class MissingNode(val nodeId: String) :
        CinematicError("Cinematic references missing node '$nodeId'")

    class UnsupportedNode(val nodeId: String, val type: String?) :
        CinematicError("Cinematic body contains unsupported node '$nodeId' of type '$type'")

    class EndNotReached(val endNodeId: String) :
        CinematicError("Cinematic end marker '$endNodeId' was not reached from the start marker")
}

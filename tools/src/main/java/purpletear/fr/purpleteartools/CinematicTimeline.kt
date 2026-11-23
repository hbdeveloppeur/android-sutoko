package purpletear.fr.purpleteartools

object CinematicTimeline {
    data class Cue(val durationMillis: Long, val nextId: Int?)

    @JvmStatic
    fun duration(firstId: Int, cue: (Int) -> Cue): Long {
        val visited = mutableSetOf<Int>()
        var id: Int? = firstId
        var duration = 0L
        while (id != null) {
            require(visited.add(id)) { "Cyclic cinematic at phrase $id" }
            val current = cue(id)
            require(current.durationMillis >= 0) { "Negative cinematic duration at phrase $id" }
            duration = Math.addExact(duration, current.durationMillis)
            id = current.nextId
        }
        return duration
    }
}

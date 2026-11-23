package purpletear.fr.purpleteartools

import android.os.Handler
import android.os.Looper
import android.os.SystemClock

/** Keeps cinematic cues on the same paused clock as their progress indicator. */
class CinematicScheduler {
    private val handler = Handler(Looper.getMainLooper())
    private val pending = mutableListOf<Task>()
    private var paused = true

    private inner class Task(val action: Runnable, var remaining: Long) : Runnable {
        var due = 0L
        override fun run() {
            pending.remove(this)
            action.run()
        }
        fun post() {
            due = SystemClock.uptimeMillis() + remaining
            handler.postDelayed(this, remaining)
        }
    }

    fun schedule(delayMillis: Long, action: Runnable) {
        val task = Task(action, delayMillis.coerceAtLeast(0))
        pending.add(task)
        if (!paused) task.post()
    }

    fun resume() {
        if (!paused) return
        paused = false
        pending.forEach { it.post() }
    }

    fun pause() {
        if (paused) return
        paused = true
        val now = SystemClock.uptimeMillis()
        pending.forEach {
            handler.removeCallbacks(it)
            it.remaining = (it.due - now).coerceAtLeast(0)
        }
    }

    fun clear() {
        handler.removeCallbacksAndMessages(null)
        pending.clear()
    }
}

package purpletear.fr.purpleteartools

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

class DelayHandler : DefaultLifecycleObserver {
    private var arrayOfCallBacks: ArrayList<CallBackHR>? = ArrayList<CallBackHR>()

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stop()
        arrayOfCallBacks = null
    }

    /**
     * Adds and runs an operation in <delay> milliseconds
     * @param name : String
     * @param delay : Int - Milliseconds
     * @param runnable : () -> Unit
     */
    fun operation(name: String, delay: Int, runnable: () -> Unit) {
        val r = object : Runnable2(name, delay) {
            override fun run() {
                runnable()
            }
        }
        add(r)
        run(r)
    }

    /**
     * Stops all delayed operation
     */
    fun stop() {
        for (callBack in arrayOfCallBacks ?: return) {
            callBack.setDone()
            callBack.handler.removeCallbacks(callBack.runnable.get() ?: continue)
        }
        arrayOfCallBacks?.clear()
    }

    /**
     * Stops a specific delayed operation
     * @param name : String - Operation's mname
     */
    fun stop(name: String) {
        var found = false
        var position = 0
        for (callBack in arrayOfCallBacks!!) {
            val r = callBack.runnable.get() ?: continue
            if (r.name == name) {
                callBack.setDone()
                callBack.handler.removeCallbacks(r)
                found = true
                break
            }
            position++
        }

        if (found) {
            arrayOfCallBacks?.removeAt(position)
        }
    }

    /**
     * Stops every operation named <mname>
     * @param name : String
     */
    fun stopEvery(name: String) {
        while (true) {
            var found = false
            var position = 0
            for (callBack in arrayOfCallBacks!!) {
                val r = callBack.runnable.get() ?: continue
                if (r.name == name) {
                    callBack.setDone()
                    callBack.handler.removeCallbacks(r)
                    found = true
                    break
                }
                position++
            }
            if (found) {
                arrayOfCallBacks?.removeAt(position)
            } else {
                break
            }
        }
    }

    /**
     * Determines if the given operation mname exists
     * @param name : String
     * return Boolean
     */
    fun has(name: String): Boolean {
        for (callBack in arrayOfCallBacks!!) {
            val r = callBack.runnable.get() ?: continue
            if (r.name == name) {
                return true
            }
        }
        return false
    }

    /**
     * Add an operation
     * @param runnable : Runnable2
     */
    private fun add(runnable: Runnable2) {
        val r = WeakReference<Runnable2>(runnable)
        arrayOfCallBacks?.add(CallBackHR(Handler(Looper.getMainLooper()), r))
    }

    /**
     * Runs an operation
     * @param runnable2 : Runnable2
     */
    private fun run(runnable2: Runnable2) {
        val callBackHR = findByRunnable(runnable2) ?: return
        val runnable = callBackHR.runnable.get() ?: return
        if (runnable.duration > 0) {
            callBackHR.handler.postDelayed(
                Runnable {
                    if (callBackHR.isDone) {
                        return@Runnable
                    }
                    callBackHR.setDone()
                    runnable.run()
                },
                runnable.duration
            )
        } else {
            callBackHR.handler.post(runnable)
            callBackHR.setDone()
        }
    }

    private fun findByRunnable(runnable: Runnable2): CallBackHR? {
        if (arrayOfCallBacks == null) {
            return null
        }

        for (callBack in arrayOfCallBacks!!) {
            val r = callBack.runnable.get()
            if (r != null && r.equals(runnable) && !callBack.isDone) {
                return callBack
            }
        }
        throw IllegalArgumentException("Handler called but not found")
    }


    internal inner class CallBackHR(var handler: Handler, var runnable: WeakReference<Runnable2>) {
        var isDone: Boolean = false
            private set

        init {
            isDone = false
        }

        fun setDone() {
            isDone = true
        }
    }

    abstract inner class Runnable2(val name: String, duration: Int) : Runnable {
        val duration: Long = duration.toLong()

        override fun equals(o: Any?): Boolean {
            if (o === this) {
                return true
            }
            if (o !is Runnable2) {
                return false
            }
            val other = o as Runnable2?
            return other!!.duration == duration && other.name == name

        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + duration.hashCode()
            return result
        }
    }

}
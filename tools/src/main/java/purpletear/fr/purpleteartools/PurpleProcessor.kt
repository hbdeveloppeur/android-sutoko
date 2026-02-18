package purpletear.fr.purpleteartools

class PurpleProcessor {
    private var index : Int = 0

    fun addProcess() {
        this.index++
    }

    fun notifyProcessFinished() {
        this.index--
    }

    fun isFinished() : Boolean {
        return this.index == 0
    }
}
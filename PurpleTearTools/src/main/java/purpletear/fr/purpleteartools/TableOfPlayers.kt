@file:Suppress("JoinDeclarationAndAssignment")

package purpletear.fr.purpleteartools

import android.app.Activity
import android.content.Context

class TableOfSoundsPlayer {
    private var players: ArrayList<PurpleExoPlayer>
    private var urlsToLoad: ArrayList<String>

    init {
        players = ArrayList()
        urlsToLoad = ArrayList()
    }

    fun addToPreloadList(url: String) {
        if (!urlsToLoad.contains(url)) {
            urlsToLoad.add(url)
        }
    }

    fun preload(context: Context) {

        urlsToLoad.forEach { url ->
            if (!exists(url)) {
                val player = PurpleExoPlayer(R.string.app_name, url, false)
                players.add(player)
                val position = players.indexOf(player)
                players[position].create(context)
            }
        }
    }

    fun isReady(url: String) : Boolean {
        if(exists(url)) {
            val position = this.index(url)
            return this.players[position].isReady
        }
        return false
    }

    fun remove(url: String) {
        var i = -1
        players.forEachIndexed { index, player ->
            if (player.url == url) {
                i = index
            }
        }

        if (-1 != i) {
            players[i].release()
            players.removeAt(i)
        }
    }


    private fun exists(url: String): Boolean {
        players.forEach { player ->
            if (player.url == url) {
                return true
            }
        }
        return false
    }

    fun isPlaying(url : String) : Boolean {
        players.forEach { player ->
            if (player.url == url) {
                return player.isPlaying()
            }
        }
        return false
    }

    fun fadeOutSound(activity: Activity, url : String, duration : Float, delayHandler : DelayHandler) {
        delayHandler.operation( "fading out $url", (0.01f * duration).toInt()) {
            // set volume
            if(!exists(url)) {
                return@operation
            }
            val index = index(url)
            val volume = this.players[index].volume()
            if( volume > 0f) {
                this.players[index].setVolume(volume - 0.01f)
                fadeOutSound(activity, url, duration, delayHandler)
            }
        }
    }
    fun fadeInSound(activity: Activity, url : String, duration : Float, delayHandler : DelayHandler) {
        delayHandler.operation( "fading out $url", (0.01f * duration).toInt()) {
            // set volume
            if(!exists(url)) {
                return@operation
            }
            val index = index(url)
            val volume = this.players[index].volume()
            if( volume < 1f) {
                this.players[index].setVolume(volume + 0.01f)
                fadeInSound(activity, url, duration, delayHandler)
            }
        }
    }

    fun play(
        url: String,
        onPlayingChangeListener: (isPlaying: Boolean) -> Unit, onEnded: () -> Unit, releaseOnEnd : Boolean = true
    ) {
        players.forEach { player ->
            if (player.url == url) {
                player.play(isPlayingListener@{ isPlaying ->
                    onPlayingChangeListener(isPlaying)
                }, onEnded@{
                    if(releaseOnEnd) {
                        player.release()
                        onEnded()
                    } else {
                        onEnded()
                        player.rollback()
                    }

                })
            }
        }
    }

    fun play(
        url: String,
        onPlayingChangeListener: (isPlaying: Boolean) -> Unit, onEnded: () -> Unit, volume : Float
    ) {
        players.forEach { player ->
            if (player.url == url) {
                player.setVolume(volume)
                player.play(isPlayingListener@{ isPlaying ->
                    onPlayingChangeListener(isPlaying)
                }, onEnded@{
                    player.release()
                    onEnded()

                })
            }
        }
    }

    fun duration(name : String) : Long {
        players.forEach {
            if(it.url == name) {
                return it.getDuration()
            }
        }
        return 0
    }

    /**
     *
     * @param name String
     * @return Long
     */
    fun position(name : String) : Long {
        players.forEach {
            if(it.url == name) {
                return it.getPosition()
            }
        }
        return 0
    }

    /**
     *
     * @param name String
     * @return Long
     */
    fun index(name : String) : Int {
        players.forEachIndexed { index, purpleExoPlayer ->
            if(purpleExoPlayer.url == name) {
                return index
            }
        }
        return 0
    }

    fun rollback(name : String) {
        players.forEach {
            if(it.url == name) {
                it.rollback()
            }
        }
    }

    fun pause(url: String) {
        players.forEach { player ->
            if (player.url == url) {
                player.setPlayWhenReady(false)
            }
        }
    }

    fun onStart(context: Context) {
        players.forEach { player ->
            player.onStart(context)
        }
    }

    fun onResume(context: Context) {
        players.forEach { player ->
            player.onResume(context)
        }
    }

    fun onPause() {
        players.forEach { player ->
            player.onPause()
        }
    }

    fun onStop() {
        players.forEach { player ->
            player.onStop()
        }
    }
}
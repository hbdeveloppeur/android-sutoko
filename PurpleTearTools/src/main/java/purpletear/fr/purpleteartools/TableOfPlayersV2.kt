@file:Suppress("JoinDeclarationAndAssignment")

package purpletear.fr.purpleteartools

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class TableOfPlayersV2(
    private val context: Context,
    private val delayHandler: DelayHandler
) : DefaultLifecycleObserver {
    private val players: ArrayList<PurpleExoPlayer> = ArrayList()
    private val urlsToLoad: HashSet<String> = HashSet()
    private var lifecycle: Lifecycle? = null

    init {
        if (context is LifecycleOwner) {
            lifecycle = context.lifecycle
            lifecycle?.addObserver(this)
        }
    }

    fun addToPreloadList(url: String) {
        synchronized(urlsToLoad) {
            urlsToLoad.add(url)
        }
    }

    fun preload() {
        val urls = synchronized(urlsToLoad) {
            ArrayList(urlsToLoad)
        }

        urls.forEach { url ->
            if (!exists(url)) {
                val player = PurpleExoPlayer(R.string.app_name, url, false)
                players.add(player)
                player.create(context)
            }
        }
    }

    fun isReady(url: String): Boolean {
        val position = index(url)
        return if (position != -1) {
            players[position].isReady
        } else {
            false
        }
    }

    fun remove(url: String) {
        val position = index(url)
        if (position != -1) {
            players[position].release()
            players.removeAt(position)
        }
    }

    private fun exists(url: String): Boolean {
        return players.any { player ->
            player.url == url
        }
    }

    fun isPlaying(url: String): Boolean {
        return players.any { player ->
            player.url == url && player.isPlaying()
        }
    }

    fun fadeOutSound(url: String, duration: Float) {
        delayHandler.operation("fading out $url", (0.01f * duration).toInt()) {
            // set volume
            val index = index(url)
            if (index != -1) {
                val player = players[index]
                val volume = player.volume()
                if (volume > 0f) {
                    player.setVolume(volume - 0.01f)
                    fadeOutSound(url, duration)
                }
            }
        }
    }

    fun fadeInSound(url: String, duration: Float) {
        delayHandler.operation("fading in $url", (0.01f * duration).toInt()) {
            // set volume
            val index = index(url)
            if (index != -1) {
                val player = players[index]
                val volume = player.volume()
                if (volume < 1f) {
                    player.setVolume(volume + 0.01f)
                    fadeInSound(url, duration)
                }
            }
        }
    }

    fun play(
        url: String,
        onPlayingChangeListener: (isPlaying: Boolean) -> Unit,
        onEnded: () -> Unit,
        releaseOnEnd: Boolean = true
    ) {
        val player = getPlayer(url)
        player?.play(
            isPlayingListener@{ isPlaying ->
                onPlayingChangeListener(isPlaying)
            },
            onEnded@{
                if (releaseOnEnd) {
                    player.release()
                    onEnded()
                } else {
                    onEnded()
                    player.rollback()
                }
            }
        )
    }

    fun play(
        url: String,
        onPlayingChangeListener: (isPlaying: Boolean) -> Unit,
        onEnded: () -> Unit,
        volume: Float
    ) {
        val player = getPlayer(url)
        player?.setVolume(volume)
        player?.play(
            isPlayingListener@{ isPlaying ->
                onPlayingChangeListener(isPlaying)
            },
            onEnded@{
                player.release()
                onEnded()
            }
        )
    }

    fun duration(name: String): Long {
        val player = getPlayer(name)
        return player?.getDuration() ?: 0
    }

    fun position(name: String): Long {
        val player = getPlayer(name)
        return player?.getPosition() ?: 0
    }

    private fun index(name: String): Int {
        return players.indexOfFirst { player ->
            player.url == name
        }
    }

    fun rollback(name: String) {
        val player = getPlayer(name)
        player?.rollback()
    }

    fun pause(url: String) {
        val player = getPlayer(url)
        player?.setPlayWhenReady(false)
    }

    override fun onStart(owner: LifecycleOwner) {
        players.forEach { player ->
            player.onStart(context)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        players.forEach { player ->
            player.onResume(context)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        players.forEach { player ->
            player.onPause()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        players.forEach { player ->
            player.onStop()
        }
    }

    private fun getPlayer(url: String): PurpleExoPlayer? {
        return players.find { player ->
            player.url == url
        }
    }
}

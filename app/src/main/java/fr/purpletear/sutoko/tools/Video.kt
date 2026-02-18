package fr.purpletear.sutoko.tools

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.VideoView

class Video {
    companion object {
        /**
         * Sets a videoView given a uri
         * @param v : The videoView
         * @param uri : The video descriptor
         * @param looping : Does the video needs to loop ?
         */
        fun put(v: VideoView?, uri: Uri?, looping: Boolean, onReady : (() -> Unit)?, onCompletion: (() -> Unit)?) {
            if (uri == null) throw AssertionError("Video.put.uri (Uri) cannot be null")
            if (v == null) throw AssertionError("Video.put.v (VideoView) cannot be null")

            v.setVideoURI(uri)
            v.start()
            v.post {
                if(onReady != null) {
                    onReady()
                }
            }
            v.setOnPreparedListener { mp -> mp.isLooping = looping }
            v.setOnCompletionListener {
                if(onCompletion != null){
                    Handler(Looper.getMainLooper()).post {
                        onCompletion()
                    }
                }
            }
            v.setOnErrorListener(object : MediaPlayer.OnErrorListener {
                override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                    Std.debug(">", what)
                    return false
                }
            })
        }

        fun put(v: VideoView?, uri: Uri?, looping: Boolean, onCompletion: (() -> Unit)?) {
            put(v, uri, looping, null, onCompletion)
        }

        fun playUrl(activity : Activity, v: VideoView?, uri: Uri?, looping: Boolean, onCompletion: (() -> Unit)?) {
            if (uri == null) throw AssertionError("Video.put.uri (Uri) cannot be null")
            if (v == null) throw AssertionError("Video.put.v (VideoView) cannot be null")

            v.setVideoURI(uri)
            v.start()
            v.setOnPreparedListener { mp -> mp.isLooping = looping }
            v.setOnCompletionListener {
                if(onCompletion != null){
                    Handler(Looper.getMainLooper()).post {
                        onCompletion()
                    }
                }
            }
        }

        /**
         * Returns the id of the raw resources given the mname of the resource and the context.
         * @param name the mname of the resource (without the extension)
         * @param context the calling activity context
         * @return id of the drawable.
         */
        fun determine(name: String, context: Context): Int {
            val id = context.resources.getIdentifier(name, "raw", context.packageName)
            return id
        }

        fun start(v: VideoView?) {
            if (v != null && !v.isPlaying) {
                v.start()
            }
        }

        /**
         * Pauses the video given a videoView
         * @param v the videow concerned
         */
        fun pause(v: VideoView?) {
            if (v != null && v.isPlaying) {
                v.pause()
            }
        }
    }
}
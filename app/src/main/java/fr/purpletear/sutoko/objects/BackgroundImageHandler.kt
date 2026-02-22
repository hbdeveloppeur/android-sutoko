package fr.purpletear.sutoko.objects

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.widget.ImageView
import androidx.annotation.Keep
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.sharedelements.GraphicsPreference

@Keep
class BackgroundImageHandler() : Parcelable {
    private var isLoading: Boolean

    private var backgroundRes: Int?
    private var backgroundUrl: String?

    init {
        isLoading = false
        backgroundRes = null
        backgroundUrl = null
    }

    constructor(source: Parcel) : this() {
        isLoading = source.readByte() == 1.toByte()
        backgroundRes = if (source.readInt() == -1) null else source.readInt()
        backgroundUrl = source.readString()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        dest.writeByte(if (isLoading) 1 else 0)
        dest.writeInt(backgroundRes ?: -1)
        dest.writeString(backgroundUrl)
    }

    /**
     * Loads an Image
     * @param imageRef Any
     * @param imageView ImageView
     * @param withRequestManager RequestManager
     * @param onLoaded Function0<Unit>
     * @param onFailure Function0<Unit>
     */
    fun load(
        imageRef: Any,
        imageView: ImageView,
        withRequestManager: RequestManager,
        onLoaded: () -> Unit,
        onFailure: () -> Unit
    ) {
        isLoading = true
        when (imageRef) {
            is Int -> {
                backgroundRes = imageRef
            }

            is String -> {
                backgroundUrl = imageRef
            }

            else -> {
                throw IllegalStateException("Type not allowed.")
            }
        }

        withRequestManager.load(imageRef)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    isLoading = false
                    onFailure()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    isLoading = false
                    onLoaded()
                    return false
                }
            })
            .apply(GraphicsPreference.getRequestOptions(GraphicsPreference.Level.DONT_CACHE))
            .into(imageView)
    }

    @Suppress("unused")
    fun isLoading(url: String): Boolean {
        return isLoading && backgroundUrl == url
    }

    @Suppress("unused")
    fun isLoading(resource: Int): Boolean {
        return isLoading && backgroundRes == resource
    }

    @Suppress("unused")
    fun readyFor(url: String): Boolean {
        return !isLoading && backgroundUrl == url
    }

    @Suppress("unused")
    fun readyFor(resource: Int): Boolean {
        return !isLoading && backgroundRes == resource
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BackgroundImageHandler> =
            object : Parcelable.Creator<BackgroundImageHandler> {
                override fun createFromParcel(source: Parcel): BackgroundImageHandler =
                    BackgroundImageHandler(source)

                override fun newArray(size: Int): Array<BackgroundImageHandler?> =
                    arrayOfNulls(size)
            }
    }
}
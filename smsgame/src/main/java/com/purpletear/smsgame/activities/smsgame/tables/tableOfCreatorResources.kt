package com.purpletear.smsgame.activities.smsgame.tables

import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import com.example.sharedelements.SutokoSharedElementsData
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.smsgame.activities.smsgame.objects.CreatorResource
import com.purpletear.smsgame.activities.userStoryLoader.SutokoError
import purpletear.fr.purpleteartools.Language
import java.lang.reflect.Type

class TableOfCreatorResources() : Parcelable {
    var isLoaded: Boolean
        private set
    private var creatorResourcesSounds: ArrayList<CreatorResource>
    private var creatorResourcesAnimations: ArrayList<CreatorResource>
    private var creatorResourcesBackgroundVideos: ArrayList<CreatorResource>
    private var creatorResourcesBackgroundImages: ArrayList<CreatorResource>

    init {
        isLoaded = false
        creatorResourcesSounds = ArrayList()
        creatorResourcesAnimations = ArrayList()
        creatorResourcesBackgroundVideos = ArrayList()
        creatorResourcesBackgroundImages = ArrayList()
    }

    enum class CreatorResourceType(val rname: String) {
        SOUNDS("Sound"),
        VIDEOS("BackgroundVideo"),
        IMAGES("BackgroundImage"),
        ANIMATIONS("Animation"),
    }

    /**
     * Loads the effects from an url
     * @param onSuccess Function0<Unit>
     * @param force Boolean
     */
    fun loadEffectList(
        onSuccess: () -> Unit,
        onError: (error: SutokoError) -> Unit,
        force: Boolean = false
    ) {

        if (!force && isLoaded) {
            Handler(Looper.getMainLooper()).post(onSuccess)
            return
        }

        val langCode: String = Language.determineLangDirectory()
        val url = SutokoSharedElementsData.getSutokoDataUrl(
            SutokoSharedElementsData.Type.CREATOR_RESOURCES,
            langCode
        )

        url.httpGet().timeout(6000).responseString { _, response, result ->
            when (result) {
                is Result.Success -> {
                    val json = result.get()
                    val map = creatorResourcesToMap(json)
                    val sounds = map[CreatorResourceType.SOUNDS.rname] ?: ArrayList()
                    val backgroundVideos = map[CreatorResourceType.VIDEOS.rname] ?: ArrayList()
                    val backgroundImages = map[CreatorResourceType.IMAGES.rname] ?: ArrayList()
                    val animations = map[CreatorResourceType.ANIMATIONS.rname] ?: ArrayList()

                    setTypes(sounds, CreatorResourceType.SOUNDS)
                    setTypes(backgroundVideos, CreatorResourceType.VIDEOS)
                    setTypes(backgroundImages, CreatorResourceType.IMAGES)
                    setTypes(animations, CreatorResourceType.ANIMATIONS)

                    fillCreatorResources(sounds, animations, backgroundVideos, backgroundImages)
                    isLoaded = true
                    Handler(Looper.getMainLooper()).post(onSuccess)
                }
                is Result.Failure -> {
                    Handler(Looper.getMainLooper()).post {
                        onError(SutokoError.CHECK_CONNECTION)
                    }
                    //  Crashlytics.log(response.data.toString())
                }
            }
        }
    }

    /**
     * Fills the field "type" of every objects
     * @param list ArrayList<CreatorResource>
     * @param type CreatorResourceType
     */
    fun setTypes(list: ArrayList<CreatorResource>, type: CreatorResourceType) {
        list.forEachIndexed { index, _ ->
            list[index].type = type.rname
        }
    }

    /**
     * Returns the list given the type
     * @param type CreatorResourceType
     * @return ArrayList<CreatorResource>
     */
    fun getList(type: CreatorResourceType): ArrayList<CreatorResource> {
        return when (type) {
            CreatorResourceType.ANIMATIONS -> creatorResourcesAnimations
            CreatorResourceType.SOUNDS -> creatorResourcesSounds
            CreatorResourceType.IMAGES -> creatorResourcesBackgroundImages
            CreatorResourceType.VIDEOS -> creatorResourcesBackgroundVideos
        }
    }

    private fun getAll(): ArrayList<CreatorResource> {
        return ArrayList<CreatorResource>().apply {
            addAll(creatorResourcesAnimations)
            addAll(creatorResourcesSounds)
            addAll(creatorResourcesBackgroundImages)
            addAll(creatorResourcesBackgroundVideos)
        }
    }

    fun getResourceBy(id: Int): CreatorResource? {
        if (!isLoaded) {
            throw IllegalStateException()
        }
        getAll().forEach { resource ->
            if (id == resource.id) {
                return resource
            }
        }
        return null
    }

    private fun creatorResourcesToMap(json: String): Map<String, ArrayList<CreatorResource>> {
        val type: Type = object :
            TypeToken<Map<String, ArrayList<CreatorResource>>>() {}.type
        return Gson().fromJson(json, type)
    }

    /**
     * Fills the Creator Resources arrays
     * @param sounds ArrayList<CreatorResource>
     * @param animations ArrayList<CreatorResource>
     * @param backgroundVideos ArrayList<CreatorResource>
     * @param backgroundImages ArrayList<CreatorResource>
     */
    private fun fillCreatorResources(
        sounds: ArrayList<CreatorResource>,
        animations: ArrayList<CreatorResource>,
        backgroundVideos: ArrayList<CreatorResource>,
        backgroundImages: ArrayList<CreatorResource>
    ) {
        this.creatorResourcesSounds = sounds
        this.creatorResourcesAnimations = animations
        this.creatorResourcesBackgroundVideos = backgroundVideos
        this.creatorResourcesBackgroundImages = backgroundImages
    }

    constructor(source: Parcel) : this() {
        isLoaded = source.readByte() == 1.toByte()
        creatorResourcesSounds =
            source.createTypedArrayList(CreatorResource.CREATOR) as ArrayList<CreatorResource>?
                ?: ArrayList()
        creatorResourcesBackgroundImages =
            source.createTypedArrayList(CreatorResource.CREATOR) as ArrayList<CreatorResource>?
                ?: ArrayList()
        creatorResourcesBackgroundVideos =
            source.createTypedArrayList(CreatorResource.CREATOR) as ArrayList<CreatorResource>?
                ?: ArrayList()
        creatorResourcesAnimations =
            source.createTypedArrayList(CreatorResource.CREATOR) as ArrayList<CreatorResource>?
                ?: ArrayList()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        dest.writeByte(if (isLoaded) 1 else 0)
        dest.writeTypedList(creatorResourcesSounds)
        dest.writeTypedList(creatorResourcesBackgroundImages)
        dest.writeTypedList(creatorResourcesBackgroundVideos)
        dest.writeTypedList(creatorResourcesAnimations)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TableOfCreatorResources> =
            object : Parcelable.Creator<TableOfCreatorResources> {
                override fun createFromParcel(source: Parcel): TableOfCreatorResources =
                    TableOfCreatorResources(source)

                override fun newArray(size: Int): Array<TableOfCreatorResources?> =
                    arrayOfNulls(size)
            }
    }
}
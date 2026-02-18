package com.purpletear.smsgame.activities.smsgame.adapter


import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.sutokosharedelements.SutokoParams
import com.purpletear.smsgame.R
import com.purpletear.smsgame.activities.smsgame.ConversationInterface
import com.purpletear.smsgame.activities.smsgame.items.PhraseChoiceAction
import com.purpletear.smsgame.activities.smsgame.items.PhraseDest
import com.purpletear.smsgame.activities.smsgame.items.PhraseDestImage
import com.purpletear.smsgame.activities.smsgame.items.PhraseEventDecoration
import com.purpletear.smsgame.activities.smsgame.items.PhraseInfo
import com.purpletear.smsgame.activities.smsgame.items.PhraseIsTyping
import com.purpletear.smsgame.activities.smsgame.items.PhraseMangaPage
import com.purpletear.smsgame.activities.smsgame.items.PhraseMe
import com.purpletear.smsgame.activities.smsgame.items.PhraseMeImage
import com.purpletear.smsgame.activities.smsgame.items.PhraseNextChapterDecoration
import com.purpletear.smsgame.activities.smsgame.items.PhraseRateDecoration
import com.purpletear.smsgame.activities.smsgame.items.PhraseVocalDecoration
import com.purpletear.smsgame.activities.smsgame.objects.ChoiceAction
import com.purpletear.smsgame.activities.smsgame.objects.MessageColor
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.objects.StoryChapter
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter
import com.purpletear.smsgame.activities.smsgame.tables.StoryType
import com.purpletear.smsgame.activities.smsgame.tables.TableOfCharacters
import com.purpletear.sutoko.game.model.Game
import purpletear.fr.purpleteartools.FingerV2

class GameConversationAdapter(
    private var context: Activity,
    var array: ArrayList<Phrase>,
    private val characters: TableOfCharacters,
    private var glide: RequestManager,
    private var storyId: Int,
    private var callback: ConversationInterface,
    var sutokoParams: SutokoParams,
    val storyType: StoryType = StoryType.OFFICIAL_STORY,
    val card: Game,
    val firstName: String
) : RecyclerView.Adapter<GameConversationAdapter.ViewHolder>() {

    var currentMessageColor: MessageColor = MessageColor()
    private var alreadyAnimated: ArrayList<Int> = ArrayList()
    var sideHandler = AdapterSideHandler(
        context,
        storyId,
        storyType == StoryType.CURRENT_USER_STORY || storyType == StoryType.OTHER_USER_STORY
    )
    var currentActionChoices: ArrayList<ChoiceAction> = ArrayList()
    private var vocalMessagePlayingIds: ArrayList<Int> = ArrayList()
    var arrayOfSeenMessages = hashMapOf<Int, PhraseDest.Companion.SeenState>()

    // The StoryCharacter who seens the main characters' message
    var seenCharacter: StoryCharacter? = null


    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (!alreadyAnimated.contains(holder.bindingAdapterPosition)) {
            holder.animate(array[holder.bindingAdapterPosition])
            alreadyAnimated.add(holder.bindingAdapterPosition)
        }
    }


    fun setIsPlaying(phrase: Phrase, value: Boolean) {
        if (value) {
            vocalMessagePlayingIds.add(phrase.id)
        } else {
            vocalMessagePlayingIds.remove(phrase.id)
        }
    }

    fun setMark(value: Int) {
        for (i in (array.size - 1) downTo 0) {
            val p = array[i]
            if (p.`is`(Phrase.Type.rate)) {
                p.seen = value
                array[i] = p
                return
            }
        }
    }

    /**
     * Inserts a phrase
     * @param p : Phrase to insert
     */
    fun insert(p: Phrase): Int {
        if (p.needsSkip()) return -1
        if (equalsLast(p)) return -1
        val position = array.size
        array.add(p)
        notifyItemInserted(position)
        return position
    }

    fun reloadLastItem() {
        if (this.array.isEmpty()) {
            return
        }
        this.notifyItemChanged(this.array.lastIndex)
    }

    private fun equalsLast(p: Phrase): Boolean {
        return array.size > 0 && p.id == array[array.size - 1].id && !p.`is`(Phrase.Type.rate) && !p.`is`(
            Phrase.Type.nextChapter
        )
    }

    fun replaceItemByPhraseId(p: Phrase) {
        if (p.needsSkip()) return
        var position: Int? = null

        this.array.forEachIndexed { index, phrase ->
            if (phrase.id == p.id) {
                position = index
            }
        }
        if (position != null) {
            array[position!!] = p
            notifyItemChanged(position!!)
        }
    }

    /**
     * Determines if the space between two elements should be removed
     *
     * @param position
     * @return
     */
    fun shouldRemoveSpaceWithPrevious(position: Int): Boolean {
        if (position == 0) return false
        if (position >= array.size) return false
        val previous = array[position - 1]
        val current = array[position]
        return previous.`is`(current.getType()) && previous.id_author == current.id_author
    }

    /**
     * Removes the last item in the ArrayList if it has the given type
     * @param typeIs : Phrase.Type
     */
    fun removeLastIf(typeIs: Phrase.Type) {
        val position = getLastIndex()
        val p = array[position]

        if (p.getType() !== typeIs) {
            return
        }

        array.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * Returns the last index of the array
     * @return Int
     */
    fun getLastIndex(): Int {
        return array.lastIndex
    }

    fun lastHasType(type: Phrase.Type): Boolean {
        if (array.size == 0) {
            return false
        }
        return array[array.lastIndex].getType() == type
    }

    private fun getPreviousItemFromPosition(index: Int): Phrase? {
        if (index == 0) {
            return null
        }
        return array[index - 1]
    }

    private fun getNextItemFromPosition(index: Int): Phrase? {
        if (index >= array.size - 1) {
            return null
        }
        return array[index + 1]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = when (val type = Phrase.Type.values()[viewType]) {

            Phrase.Type.isTyping -> PhraseIsTyping.LAYOUT_ID
            Phrase.Type.dest -> PhraseDest.LAYOUT_ID
            Phrase.Type.imageMe -> PhraseMeImage.LAYOUT_ID
            Phrase.Type.image -> PhraseDestImage.LAYOUT_ID
            Phrase.Type.me -> PhraseMe.LAYOUT_ID
            Phrase.Type.makeChoice -> R.layout.sutoko_phrase_choice
            Phrase.Type.info -> R.layout.sutoko_phrase_info
            Phrase.Type.end -> R.layout.sutoko_sms_game_phrase_end
            Phrase.Type.rate -> R.layout.sutoko_item_rate_game
            Phrase.Type.actionChoice -> PhraseChoiceAction.LAYOUT_ID
            Phrase.Type.mangaPagePreview -> PhraseMangaPage.LAYOUT_ID
            Phrase.Type.event -> PhraseEventDecoration.LAYOUT_ID
            Phrase.Type.nextChapter -> PhraseNextChapterDecoration.LAYOUT_ID
            Phrase.Type.vocal -> PhraseVocalDecoration.layoutDestId
            Phrase.Type.vocalMe -> PhraseVocalDecoration.layoutId

            else -> throw IllegalStateException("ERROR on story $storyId when phrase type $type")
        }

        return ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        val p = array[position]
        if (StoryType.CURRENT_USER_STORY == storyType || storyType == StoryType.OTHER_USER_STORY) {
            when (Phrase.Type.values()[p.getType().ordinal]) {
                Phrase.Type.me -> if (sideHandler.hasOnLeft(p.id)) {
                    return Phrase.Type.dest.ordinal
                } else {
                    return Phrase.Type.me.ordinal
                }

                else -> {}
            }
        }
        return p.getType().ordinal
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(
            array[position],
            getPreviousItemFromPosition(position),
            getNextItemFromPosition(position)
        )
    }

    private fun lastPosition(fromPosition: Int): Int {
        return if (fromPosition == 0) {
            0
        } else fromPosition - 1
    }

    override fun getItemCount() = array.size


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        fun animate(p: Phrase) {
            when (Phrase.Type.values()[itemViewType]) {
                Phrase.Type.dest -> {
                    PhraseDest.animate(itemView)
                }

                Phrase.Type.me -> {
                    PhraseMe.animate(itemView)
                }

                Phrase.Type.isTyping -> {
                    val character = characters.getCharacter(p.id_author)
                    PhraseIsTyping.animate(itemView, character.isMainCharacter)
                }

                else -> {}
            }
        }

        fun display(p: Phrase, previous: Phrase?, next: Phrase?) {
            when (Phrase.Type.values()[itemViewType]) {
                Phrase.Type.me -> {
                    val character = characters.getCharacter(p.id_author)
                    PhraseMe.design(
                        itemView,
                        context,
                        glide,
                        storyId,
                        currentMessageColor,
                        p,
                        character,
                        seenCharacter,
                        previous,
                        next,
                        firstName,
                        storyType,
                        arrayOfSeenMessages[p.id]
                    )
                    FingerV2.register(itemView, R.id.sutoko_phrase_me_avatar_image) {
                        callback.onProfilePictureTouched(
                            itemView.findViewById(R.id.sutoko_phrase_me_avatar_image),
                            character.id
                        )
                    }
                }

                Phrase.Type.rate -> {
                    PhraseRateDecoration.design(itemView, glide, card, callback, p)
                }

                Phrase.Type.isTyping -> {
                    val character = characters.getCharacter(p.id_author)
                    PhraseIsTyping.design(itemView, character.isMainCharacter, currentMessageColor)
                }

                Phrase.Type.dest -> {
                    val character = characters.getCharacter(p.id_author)
                    PhraseDest.design(
                        itemView,
                        context,
                        glide,
                        storyId,
                        p,
                        character,
                        previous,
                        next,
                        currentMessageColor,
                        firstName,
                        storyType
                    )
                    FingerV2.register(itemView, R.id.sutoko_phrase_dest_avatar_image) {
                        callback.onProfilePictureTouched(
                            itemView.findViewById(R.id.sutoko_phrase_dest_avatar_image),
                            character.id
                        )
                    }
                }

                Phrase.Type.imageMe -> {
                    PhraseMeImage.design(itemView, context, storyId, glide, p)
                }

                Phrase.Type.image -> {
                    PhraseDestImage.design(itemView, context, storyId, glide, p)
                }

                Phrase.Type.info -> {
                    PhraseInfo.design(itemView, p)
                }

                Phrase.Type.actionChoice -> {
                    PhraseChoiceAction.design(
                        context,
                        storyId,
                        itemView,
                        glide,
                        currentActionChoices
                    ) { pressedPhraseId ->
                        callback.onActionChoicePressed(pressedPhraseId)
                    }
                }

                Phrase.Type.mangaPagePreview -> {
                    PhraseMangaPage.design(itemView, glide)
                    PhraseMangaPage.setListener(itemView) {
                        callback.onMangaPageButtonPressed(p.sentence.replace(" ", ""))
                    }
                }

                Phrase.Type.event -> {
                    PhraseEventDecoration.design(context, storyId, glide, itemView, p)
                }

                Phrase.Type.nextChapter -> {
                    val chapterNumber = try {
                        StoryChapter.numberFromCode(p.code ?: "")
                    } catch (e: IllegalArgumentException) {
                        1
                    }
                    PhraseNextChapterDecoration.design(
                        context,
                        itemView,
                        glide,
                        callback,
                        card,
                        chapterNumber
                    )
                }

                Phrase.Type.vocal, Phrase.Type.vocalMe -> {
                    val character = characters.getCharacter(p.id_author)
                    PhraseVocalDecoration.design(
                        context,
                        itemView,
                        glide,
                        storyId,
                        p,
                        vocalMessagePlayingIds.contains(p.id),
                        previous,
                        character
                    )
                    FingerV2.register(
                        itemView.findViewById(R.id.sutoko_phrase_vocal_me_button_hitbox),
                        null
                    ) {
                        (context as SmsGamePhraseVocalListener).onPressed(p, bindingAdapterPosition)
                    }
                }

                else -> {

                }
            }
        }
    }
}
/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package fr.purpletear.friendzone2.activities.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import fr.purpletear.friendzone2.configs.Phrase
import fr.purpletear.friendzone2.phrases.*
import fr.purpletear.friendzone2.tables.TableOfCharacters
import java.lang.IllegalStateException

class GameConversationAdapter(private var context: Context, private var array: ArrayList<Phrase>, var characters: TableOfCharacters, private var glide: RequestManager, private var callback : MainInterface) : RecyclerView.Adapter<GameConversationAdapter.ViewHolder>() {
    var currentPlayingSound = ""
    var backgroundMediaId: String = ""
    var isNightMode : Boolean = false
    var isPhoneMode : Boolean = false
    fun set(value : Boolean) {
        isNightMode = value
        notifyDataSetChanged()
    }

    /**
     * Inserts a phrase
     * @param p : Phrase to insert
     */
    fun insert(p: Phrase) : Int {
        if (p.needsSkip()) return -1
        val position = array.size
        array.add(p)
        notifyItemInserted(position)
        return position
    }



    /**
     * Inserts with a specific type
     * @param p : Phrase
     * @param type : Phrase.Type
     */
    fun insert(p: Phrase, type : Phrase.Type) {
        p.type = Phrase.determineTypeCode(type)
        insert(p)
    }

    fun clear() {
        array.clear()
        notifyDataSetChanged()
    }

    /**
     * Removes the last phrase.
     */
    fun editLast(p: Phrase, type: Phrase.Type) {
        val position = array.size - 1
        p.setType(type)
        array[position] = p
        notifyItemChanged(position)
    }

    /**
     * Removes the last phrase if needed
     */
    fun removeIfLastIs(type: Phrase.Type) {
        val position = array.size - 1
        if (-1 == position) {
            return
        }
        if (array[position].getType() !== type) {
            return
        }
        array.removeAt(position)
        notifyItemRemoved(position)
    }


    /**
     * Sets the last message as meSeen
     */
    fun setLastSeen() {
        val position = array.size - 1
        val last = array[position]
        last.setType(Phrase.Type.meSeen)
        array[array.size - 1] = last
        notifyItemChanged(position)
    }


    /**
     * Returns the last item of the array
     *
     * @return Phrase
     */
    fun getLastItem(): Phrase {
        val size = array.size
        return array[size - 1]
    }

    /**
     * Returns all Phrases
     */
    fun getAll() = array

    fun setArray(array : ArrayList<Phrase>) {
        this.array = array
    }

    /**
     * Removes the last phrase if needed
     */
    private fun lastIs(type: Phrase.Type): Boolean {
        val position = array.size - 1
        return -1 != position && array[position].getType() == type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameConversationAdapter.ViewHolder {
        val layoutId = when (Phrase.determineTypeEnum(viewType)) {

            Phrase.Type.dest -> PhraseDest.getLayoutId()
            Phrase.Type.me -> PhraseMe.getLayoutId()
            Phrase.Type.typing -> PhraseTyping.getLayoutId()
            Phrase.Type.meTyping -> PhraseMeTyping.getLayoutId()
            Phrase.Type.meSeen -> PhraseMeSeen.getLayoutId()
            Phrase.Type.noSignal -> PhraseNoSignal.layoutId
            Phrase.Type.image -> PhraseImage.layoutId
            Phrase.Type.info -> PhraseInfo.layoutId
            Phrase.Type.nextChapter -> PhraseNextChapter.layoutId
            Phrase.Type.vocal -> PhraseVocal.getLayoutId()
            Phrase.Type.trophy -> com.example.sharedelements.R.layout.inc_trophy_unlocked

            else -> throw IllegalStateException("ERROR")
        }

        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(layoutId, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        if (position >= array.size) {
            return Phrase.determineTypeCode(Phrase.Type.undetermined)
        }

        return array[position].getIntType()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.display(array[position])
    }

    override fun getItemCount() = array.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun display(p: Phrase) {
            when (Phrase.determineTypeEnum(itemViewType)) {
                Phrase.Type.typing -> PhraseTyping.design(context, characters, p, itemView)
                Phrase.Type.noSignal -> PhraseNoSignal.design(context, p, itemView, glide)
                Phrase.Type.meTyping -> PhraseMeTyping.design(context, characters, p, itemView)
                Phrase.Type.dest -> PhraseDest.design(context, glide, characters, p, itemView, isNightMode, isPhoneMode)
                Phrase.Type.me -> PhraseMe.design(context, p, characters, itemView, isNightMode, isPhoneMode)
                Phrase.Type.meSeen -> PhraseMeSeen.design(context, p, itemView)
                Phrase.Type.image -> PhraseImage.design(context, glide, characters, p, itemView)
                Phrase.Type.info -> PhraseInfo.design(context, p, itemView, backgroundMediaId as String)
                Phrase.Type.nextChapter -> PhraseNextChapter.design(context, itemView, backgroundMediaId as String)
                Phrase.Type.vocal -> {
                    PhraseVocal.design(context, glide, characters, p, itemView, currentPlayingSound)
                    PhraseVocal.listener(context, itemView, p, callback)

                }
                Phrase.Type.trophy -> {

                }
                else -> {

                }
            }
        }
    }
}
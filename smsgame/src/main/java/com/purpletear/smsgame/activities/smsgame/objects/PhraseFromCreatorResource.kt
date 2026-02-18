package com.purpletear.smsgame.activities.smsgame.objects

import com.purpletear.smsgame.activities.smsgame.tables.TableOfCreatorResources
import java.util.*

object PhraseFromCreatorResource {

    fun getCode(resourceTypeName: String) = "resource_$resourceTypeName".lowercase(Locale.ENGLISH)
    private fun getSentence(resource: CreatorResource) = "[${resource.id}]"

    /**
     * Creates a Phrase from a resource file
     * @param resource CreatorResource
     * @param phraseId Int
     * @param authorId Int
     * @return Phrase
     */
    fun getPhraseFromResource(
        resource: CreatorResource,
        phraseId: Int,
        authorId: Int = -1
    ): Phrase {
        val code: String = getCode(resource.type)
        val sentence: String = getSentence(resource)
        val p = Phrase(phraseId, authorId, Phrase.Type.effect, sentence)
        p.code = code
        return p
    }

    fun determinePhraseResourceType(phrase: Phrase): TableOfCreatorResources.CreatorResourceType {
        TableOfCreatorResources.CreatorResourceType.values().forEach { type ->
            if (getCode(type.rname) == phrase.code?.lowercase(Locale.ENGLISH)) {
                return type
            }
        }
        return TableOfCreatorResources.CreatorResourceType.IMAGES
    }

    fun determineResourceType(resource: CreatorResource?): TableOfCreatorResources.CreatorResourceType {
        TableOfCreatorResources.CreatorResourceType.values().forEach { type ->
            if (type.rname == resource?.type) {
                return type
            }
        }
        return TableOfCreatorResources.CreatorResourceType.IMAGES
    }


}
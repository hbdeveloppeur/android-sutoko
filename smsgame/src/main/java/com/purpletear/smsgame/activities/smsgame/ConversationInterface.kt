package com.purpletear.smsgame.activities.smsgame

import android.widget.ImageView
import com.purpletear.smsgame.activities.smsgame.objects.ChoiceAction
import com.purpletear.smsgame.activities.smsgame.objects.CreatorResource
import com.purpletear.smsgame.activities.smsgame.objects.Phrase
import com.purpletear.smsgame.activities.smsgame.objects.StoryCharacter

interface ConversationInterface {

    fun onFilterFadeOut(delay: Int, duration: Int)
    fun onUserInputRequestFound(symboleName: String)
    fun onFilterFadeIn(delay: Int, duration: Int)
    fun onSoundFound(soundNameWithExtension: String, channel: Int, isLooping: Boolean, delay: Int)
    fun onNarratorSentenceFound(
        sentence: String,
        mode: Phrase.NarratorSentenceMode,
        delay: Int,
        duration: Int
    )

    fun onHeaderUpdateFound(
        imageNameWithExtension: String,
        title: String,
        subTitle: String,
        type: Phrase.PhraseUpdateHeaderImageType,
        delay: Int
    )

    fun onHeaderGraphicsUpdateFound(
        colorHexaCode: String,
        colorAlphaPercent: Int,
        isIconVisible: Boolean,
        delay: Int
    )

    fun onFakeNotificationFound(
        title: String,
        description: String,
        imageName: String,
        actionText: String,
        duration: Int
    )

    fun onNextChapterFound()
    fun onPhraseInfoFound(phrase: Phrase)
    fun onSimpleMessageFound(phrase: Phrase)
    fun onImageMessageFound(imageNameWithExtension: String, characterId: Int, delay: Int)
    fun onBackgroundVideoFound(videoNameWithExtension: String, delay: Int)
    fun onBackgroundImageFound(imageNameWithExtension: String, delay: Int)
    fun onBackgroundColorFound(colorHexaCode: String, delay: Int)
    fun onElementVisibilityUpdateFound(
        element: Phrase.UpdateElementVisibilityType,
        visibilityValue: Phrase.VisibilityValue,
        delay: Int,
        duration: Int
    )

    fun onPhoneVibrateFound(numberOfVibrations: Int, delay: Int)
    fun onHesitatingCharacterFound(characterId: Int, delay: Int, duration: Int)
    fun onChoiceActionsFound(actions: ArrayList<ChoiceAction>)
    fun onMemoryToSaveFound(
        phraseId: Int,
        name: String,
        value: String,
        chapterNumber: Int,
        coins: Int
    )

    fun onChoicesToMakeFound(array: ArrayList<Phrase>)
    fun onColorsUpdateFound(element: Phrase.ColorsUpdateElement, colorHexaCode: String, delay: Int)
    fun onFilterUpdateFound(colorHexaCode: String, colorAlphaPercent: Int, delay: Int)
    fun onPhraseInserted()
    fun onRegisterScoreFound(isWin: Boolean)
    fun onVocalMessageFound(phrase: Phrase)
    fun conversationFinished()
    fun onProfilePictureTouched(imageView: ImageView, characterId: Int)
    fun onNotificationFound(
        storyId: Int,
        character: StoryCharacter,
        textContent: String,
        inXDays: Int,
        atHour: Int,
        atMin: Int,
        code: String
    )

    fun onBackgroundVideoCreatorResourceFound(creatorResource: CreatorResource)
    fun onBackgroundImageCreatorResourceFound(creatorResource: CreatorResource)
    fun onSoundCreatorResourceFound(creatorResource: CreatorResource)
    fun onRateStory(mark: Int)
    fun onRateStoryButtonBackPressed()
    fun onActionChoicePressed(phraseId: Int)
    fun onMangaPageButtonPressed(filename: String)
    fun onIntroFound(id: Int)
    fun onNextChapterPressed()

}
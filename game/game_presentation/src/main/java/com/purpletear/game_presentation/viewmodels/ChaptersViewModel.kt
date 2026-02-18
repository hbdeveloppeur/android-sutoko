package com.purpletear.game_presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sutokosharedelements.utils.UiText
import com.purpletear.core.presentation.extensions.executeFlowUseCase
import com.purpletear.game_presentation.R
import com.purpletear.game_presentation.model.ChapterState
import com.purpletear.game_presentation.model.ChapterWithState
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.GetGameUseCase
import com.purpletear.sutoko.game.usecase.ObserveCurrentChapterUseCase
import com.purpletear.sutoko.game.usecase.SetCurrentChapterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.PopUpIconDrawable
import fr.purpletear.sutoko.popup.domain.PopUpUserInteraction
import fr.purpletear.sutoko.popup.domain.SutokoPopUp
import fr.purpletear.sutoko.popup.domain.usecase.GetPopUpInteractionUseCase
import fr.purpletear.sutoko.popup.domain.usecase.ShowPopUpUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Inject

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    private val getGameUseCase: GetGameUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val observeCurrentChapterUseCase: ObserveCurrentChapterUseCase,
    private val setCurrentChapterUseCase: SetCurrentChapterUseCase,
    private val showPopUpUseCase: ShowPopUpUseCase,
    private val observeInteractionUseCase: GetPopUpInteractionUseCase,
    private val symbols: TableOfSymbols,
) : ViewModel() {

    private val _gameId: Int = checkNotNull(savedStateHandle["gameId"])

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    private val _chapters = MutableStateFlow<List<ChapterWithState>>(emptyList())
    val chapters: StateFlow<List<ChapterWithState>> = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter.asStateFlow()

    init {
        symbols.gameId = _gameId
        loadGame()
        loadChaptersAndCurrentChapter()
    }

    private fun getChapterState(chapter: Chapter): ChapterState {
        val isPlayed = symbols.userPlayedChapter(chapter.getCode())
        val isCurrent = currentChapter.value?.id == chapter.id

        return when {
            isCurrent -> ChapterState.Current
            isPlayed -> ChapterState.Played
            else -> ChapterState.Locked
        }
    }

    internal fun onClickChapter(chapter: Chapter) {
        val popUp = SutokoPopUp(
            title = UiText.StringResource(R.string.game_chapters_goto_title, chapter.number),
            description = UiText.StringResource(R.string.game_chapters_goto_description),
            icon = PopUpIconDrawable(fr.purpletear.sutoko.shop.presentation.R.drawable.account_creation_character),
            buttonText = UiText.StringResource(R.string.game_chapters_continue)
        )
        val tag = showPopUpUseCase(popUp)

        executeFlowUseCase({
            observeInteractionUseCase(tag)
        }, onStream = { interaction ->
            when (interaction.event) {
                PopUpUserInteraction.Confirm -> {
                    viewModelScope.launch {
                        setCurrentChapterUseCase(_gameId, chapter)
                        _currentChapter.value = chapter
                        updateStates()
                        filterChapters()
                    }
                }

                PopUpUserInteraction.Dismiss -> {
                    // User dismissed the popup, do nothing
                }

                else -> {}
            }
        })
    }

    private fun loadGame() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getGameUseCase(_gameId).collect { result ->
                    result.onSuccess { game ->
                        _game.value = game
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun filterChapters() {
        _chapters.value = filterUniqueChapterNumbers(_chapters.value)
    }

    private fun loadChaptersAndCurrentChapter() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1280)
            try {
                // First initialize the current chapter observation
                _currentChapter.value = observeCurrentChapterUseCase(_gameId).value

                // Observe changes to the current chapter
                viewModelScope.launch {
                    observeCurrentChapterUseCase(_gameId).collect { currentChapter ->
                        _currentChapter.value = currentChapter
                        // Update chapter states when current chapter changes
                        if (_chapters.value.isNotEmpty()) {
                            updateStates()
                            filterChapters()
                        }
                    }
                }

                // Then load and process chapters with the current chapter information
                getChaptersUseCase(_gameId).collect { result ->
                    result.onSuccess { chaptersList ->
                        _chapters.value = toChaptersWithState(chaptersList)
                        filterChapters()
                        _isLoading.value = false
                    }

                    result.onFailure {
                        _isLoading.value = false
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun toChaptersWithState(chapters: List<Chapter>): List<ChapterWithState> {
        return chapters.map {
            ChapterWithState(
                chapter = it,
                state = getChapterState(it)
            )
        }
    }

    private fun updateStates() {
        _chapters.value = _chapters.value.map {
            it.copy(state = getChapterState(it.chapter))
        }
    }

    private fun filterUniqueChapterNumbers(chapters: List<ChapterWithState>): List<ChapterWithState> {
        return chapters
            .sortedWith(compareBy {
                when (it.state) {
                    ChapterState.Played -> 0
                    ChapterState.Current -> 1
                    ChapterState.Locked -> 2
                }
            })
            .distinctBy { it.chapter.number }
            .sortedWith(compareBy {
                it.chapter.number
            })
    }
}

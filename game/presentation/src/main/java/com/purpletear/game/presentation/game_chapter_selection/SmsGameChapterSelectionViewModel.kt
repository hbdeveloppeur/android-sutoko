package com.purpletear.game.presentation.game_chapter_selection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.game.presentation.R
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.usecase.GetChaptersUseCase
import com.purpletear.sutoko.game.usecase.SelectChapterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

internal data class ChapterSelectionUiState(
    val chapters: List<Chapter> = emptyList(),
    val currentChapterCode: String = "",
    val isLoading: Boolean = true,
    val isSelecting: Boolean = false,
    val errorMessage: String? = null,
)

internal sealed class ChapterSelectionEvent {
    data object NavigateBack : ChapterSelectionEvent()
}

@HiltViewModel
internal class SmsGameChapterSelectionViewModel @Inject constructor(
    private val getChapters: GetChaptersUseCase,
    private val selectChapter: SelectChapterUseCase,
    private val makeToastService: MakeToastService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChapterSelectionUiState())
    val uiState: StateFlow<ChapterSelectionUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChapterSelectionEvent>(Channel.BUFFERED)
    val events: Flow<ChapterSelectionEvent> = _events.receiveAsFlow()

    fun initialize(gameId: String, currentChapterCode: String) {
        _uiState.update { it.copy(currentChapterCode = currentChapterCode) }
        loadChapters(gameId)
    }

    fun onChapterSelected(gameId: String, chapter: Chapter) {
        if (_uiState.value.isSelecting) return
        _uiState.update { it.copy(isSelecting = true) }

        viewModelScope.launch {
            selectChapter(gameId, chapter.code)
                .onSuccess {
                    makeToastService(R.string.game_chapter_selection_success)
                    _events.send(ChapterSelectionEvent.NavigateBack)
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to select chapter ${chapter.code} for game $gameId", error)
                    _uiState.update {
                        it.copy(
                            isSelecting = false,
                            errorMessage = error.message,
                        )
                    }
                }
        }
    }

    private fun loadChapters(gameId: String) {
        getChapters(gameId)
            .onEach { result ->
                result.fold(
                    onSuccess = { chapters ->
                        _uiState.update {
                            it.copy(
                                chapters = chapters.sortedBy { chapter -> chapter.number },
                                isLoading = false,
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to load chapters for game $gameId", error)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message,
                            )
                        }
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    companion object {
        private const val TAG = "ChapterSelectionVM"
    }
}

package fr.purpletear.sutoko.screens.players_ranks.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.purpletear.core.presentation.extensions.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.data.remote.player_rank.GetPlayerRankUseCase
import fr.purpletear.sutoko.screens.players_ranks.PlayersRankEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PlayersRankViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPlayerRankUseCase: GetPlayerRankUseCase
) : ViewModel() {


    private val _state: MutableState<PlayersRankState> = mutableStateOf(PlayersRankState())
    val state: State<PlayersRankState>
        get() {
            return _state
        }

    init {
        getPlayerRanks()
    }

    fun onEvent(event: PlayersRankEvent) {
        when (event) {
            is PlayersRankEvent.SearchPlayersRank -> {
                val text = event.text
                if (text.length > 2) {
                    searchPlayersRank(text)
                } else {
                    resetList()
                }
            }

            is PlayersRankEvent.TextChanged -> {
                val text = event.text
                if (text.length < 3) {
                    resetList()
                }
            }

            is PlayersRankEvent.ClearSearch -> {
                resetList()
            }
        }
    }

    private fun resetList() {
        _state.value = _state.value.copy(
            authorsRank = _state.value.initiAuthorsRank,
            isLoading = false
        )
    }

    private fun searchPlayersRank(text: String) {
        getPlayerRankUseCase.searchPlayerRank(text).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    this._state.value = this._state.value.copy(
                        authorsRank = result.data ?: listOf(),
                        isLoading = false,
                    )
                }

                is Resource.Error -> {

                }

                is Resource.Loading -> {
                    this._state.value = this._state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getPlayerRanks() {
        getPlayerRankUseCase.getTop100PlayerRanks().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    this._state.value = this._state.value.copy(
                        initiAuthorsRank = result.data ?: listOf(),
                        authorsRank = result.data ?: listOf(),
                        isLoading = false
                    )
                }

                is Resource.Error -> {

                }

                is Resource.Loading -> {
                    this._state.value = this._state.value.copy(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}
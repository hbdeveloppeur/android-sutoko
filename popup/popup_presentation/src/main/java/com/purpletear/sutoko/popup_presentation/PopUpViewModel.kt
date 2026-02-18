package com.purpletear.sutoko.popup_presentation


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.purpletear.sutoko.popup.domain.repository.PopUpRepository
import fr.purpletear.sutoko.popup.domain.usecase.IsVisiblePopUpUseCase
import javax.inject.Inject

@HiltViewModel
internal class PopUpViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val getIsVisiblePopUpUseCase: IsVisiblePopUpUseCase,
    val popUpRepository: PopUpRepository,
) : ViewModel() {

    init {}
}
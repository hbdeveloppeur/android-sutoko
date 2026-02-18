package com.purpletear.aiconversation.presentation.screens.character.add_character

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.rememberImagePicker
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import com.purpletear.aiconversation.domain.enums.Gender
import com.purpletear.aiconversation.domain.enums.ProcessStatus
import com.purpletear.aiconversation.presentation.R
import com.purpletear.aiconversation.presentation.component.button.ButtonComposable
import com.purpletear.aiconversation.presentation.component.button.ButtonIconComposable
import com.purpletear.aiconversation.presentation.component.button.ButtonTheme
import com.purpletear.aiconversation.presentation.component.divider.DividerComposable
import com.purpletear.aiconversation.presentation.component.image_card.FullImageCardComposable
import com.purpletear.aiconversation.presentation.component.input.text.InputTextComposable
import com.purpletear.aiconversation.presentation.component.multiline_input.TextAreaComposable
import com.purpletear.aiconversation.presentation.component.options.CircularOptionsTab
import com.purpletear.aiconversation.presentation.component.title.Title
import com.purpletear.aiconversation.presentation.navigation.AiConversationRouteDestination
import com.purpletear.aiconversation.presentation.screens.character.add_character.components.AddCharacterHeader
import com.purpletear.aiconversation.presentation.screens.character.add_character.components.AddCharacterProcessingPage
import com.purpletear.aiconversation.presentation.screens.character.add_character.viewmodels.AddCharacterLastNameViewModel
import com.purpletear.aiconversation.presentation.screens.character.add_character.viewmodels.AddCharacterNameViewModel
import com.purpletear.aiconversation.presentation.screens.character.add_character.viewmodels.AddCharacterViewModel
import com.purpletear.aiconversation.presentation.screens.media.image_generator.state.ImageGeneratorState
import com.purpletear.aiconversation.presentation.sealed.NavigationEvent
import com.purpletear.aiconversation.presentation.theme.BlueBackground
import com.purpletear.aiconversation.presentation.validators.NameValidator
import com.purpletear.aiconversation.presentation.validators.ValidationResult
import kotlinx.coroutines.launch


@Composable
fun AddCharacterScreen(viewModel: AddCharacterViewModel, navController: NavHostController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueBackground)
    ) {
        AddCharacterHeader(
            Modifier.align(Alignment.TopCenter)
        )

        val lifecycleOwner = LocalLifecycleOwner.current
        val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

        LaunchedEffect(lifecycleState) {
            when (lifecycleState) {
                Lifecycle.State.RESUMED -> {
                    viewModel.onResume()
                }

                else -> {}
            }
        }
        // val items by viewModel.navigationEvents.collectAsState(initial = null)


        DisposableEffect(lifecycleOwner) {
            val job = lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.navigationEvents.collect { event ->
                        when (event) {
                            is NavigationEvent.NavigateBack -> {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
            onDispose { job.cancel() }
        }


        val listState = rememberLazyListState()
        var backgroundAlpha by remember { mutableFloatStateOf(0f) }
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp

        val density = LocalDensity.current
        val max = with(density) { screenWidth.toPx() }


        Box(
            Modifier
                .fillMaxSize()
                .alpha(backgroundAlpha)
                .background(BlueBackground)
        )

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemScrollOffset }
                .collect { offset ->
                    if (listState.layoutInfo.visibleItemsInfo[0].index == 0) {
                        backgroundAlpha = 0.95f - (max - offset).coerceIn(0f, max) / max

                    }
                }
        }

        val nameInputTextViewModel: AddCharacterNameViewModel = viewModel()
        val lastNameInputTextViewModel: AddCharacterLastNameViewModel = viewModel()

        List(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth(),
            state = listState,
            contentPaddingValues = PaddingValues(bottom = 62.dp),
            navController = navController,
            nameInputTextViewModel = nameInputTextViewModel,
            lastNameInputTextViewModel = lastNameInputTextViewModel
        )

        AnimatedVisibility(
            visible = viewModel.submitCharacterLoadingState.value != ProcessStatus.INITIAL,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AddCharacterProcessingPage(
                Modifier.fillMaxSize(),
                gender = if (viewModel.characterGender.value == Gender.Female.n) Gender.Female else Gender.Male,
                name = "${nameInputTextViewModel.text.value} ${lastNameInputTextViewModel.text.value}",
                media = viewModel.avatarBannerPair.value?.avatar,
                processState = viewModel.submitCharacterLoadingState.value,
                onClickContinue = {
                    navController.popBackStack()
                }
            )
        }
    }
}


@Composable
private fun List(
    viewModel: AddCharacterViewModel,
    modifier: Modifier,
    state: LazyListState,
    contentPaddingValues: PaddingValues,
    navController: NavHostController,
    nameInputTextViewModel: AddCharacterNameViewModel,
    lastNameInputTextViewModel: AddCharacterLastNameViewModel
) {
    val imageCropper = rememberImageCropper()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imagePicker = rememberImagePicker(onImage = { uri ->
        scope.launch {
            when (val result = imageCropper.crop(uri, context)) {
                CropResult.Cancelled -> {}
                is CropError -> {}
                is CropResult.Success -> {
                    viewModel.onImageImported(image = result.bitmap)
                }
            }
        }
    })
    val cropState = imageCropper.cropState
    val focusManager = LocalFocusManager.current

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }) {
        if (cropState != null) ImageCropperDialog(state = cropState)

        LazyColumn(
            state = state,
            modifier = Modifier
                .then(modifier),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPaddingValues
        ) {
            item(key = "SquareFirstItem") {
                Spacer(
                    modifier = Modifier
                        .height(LocalConfiguration.current.screenWidthDp.dp * 0.7f)

                )
            }

            item(key = "Title-1") {
                Title(
                    modifier = Modifier
                        .fillMaxWidth(0.92f),
                    title = stringResource(R.string.ai_conversation_title_create_character),
                    subtitle = stringResource(R.string.ai_conversation_subtitle_create_character),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item(key = "Gender-selection") {
                CircularOptionsTab(
                    Modifier.fillMaxWidth(0.92f),
                    items = mapOf(
                        Gender.Female.n to stringResource(R.string.ai_conversation_gender_female),
                        Gender.Male.n to stringResource(R.string.ai_conversation_gender_male)
                    ),
                    selectedItemId = viewModel.characterGender.value,
                    onClick = viewModel::onCharacterGenderChange
                )
            }

            item(key = "InputTextComposable") {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InputTextComposable(
                        modifier = Modifier
                            .fillMaxWidth(0.92f),
                        label = stringResource(R.string.ai_conversation_sutoko_first_name),
                        value = nameInputTextViewModel.text.value,
                        isErrorEnabled = nameInputTextViewModel.validationResult.value != ValidationResult.Success,
                        onValueChange = nameInputTextViewModel::onValueChange,
                        onClickErrorBadge = {
                            nameInputTextViewModel.validationResult.value.message?.let {
                                viewModel.toast(it)
                            }
                        },
                    )
                }
            }

            item(key = "Title-2") {
                Title(
                    modifier = Modifier
                        .fillMaxWidth(0.92f),
                    title = stringResource(R.string.ai_conversation_pick_one_graphic_style),
                    subtitle = stringResource(R.string.ai_conversation_how_do_you_want_them_to_appear),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            item(key = "CircularOptionsTab") {
                CircularOptionsTab(
                    Modifier.fillMaxWidth(0.92f),
                    items = viewModel.charactersStyles.value.associate { it.id to it.label },
                    selectedItemId = viewModel.selectedCharacterStyle.value?.id ?: 1,
                    onClick = f@{ id ->
                        viewModel.onCharacterStyleSelected(viewModel.charactersStyles.value.find { s -> s.id == id }
                            ?: return@f)
                    }
                )
            }

            item(key = "Title-3") {
                Title(
                    modifier = Modifier
                        .fillMaxWidth(0.92f),
                    title = stringResource(R.string.ai_conversation_describe_your_character),
                    subtitle = stringResource(R.string.ai_conversation_character_description_hint),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            item(key = "Title-character-description") {
                TextAreaComposable(
                    text = viewModel.characterDescription.value,
                    Modifier
                        .fillMaxWidth(0.92f),
                    placeholder = stringResource(R.string.ai_conversation_description_placeholder),
                    onChange = viewModel::onCharacterDescriptionChange,
                    backgroundColor = Color(0xFF111B27),
                    strokeColor = Color(0xFF537399),
                    displayCount = true
                )
            }

            item(key = "Divider-4") {
                DividerComposable(Modifier.padding(vertical = 12.dp))
            }


            item(key = "Preview-Avatar") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FullImageCardComposable(
                        modifier = Modifier
                            .fillMaxWidth(0.92f),
                        avatarBannerPair = viewModel.avatarBannerPair.value,
                        isLoading = viewModel.isLoadingAvatarAndBannerPair.value,
                        avatarBitmap = viewModel.importedAvatarBitmap.value
                    )


                    Row(
                        Modifier
                            .fillMaxWidth(0.92f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ButtonIconComposable(
                            Modifier.weight(1f),
                            drawableId = R.drawable.vec_image,
                            text = "Import",
                            onClick = {
                                imagePicker.pick()
                            }
                        )
                        ButtonIconComposable(
                            Modifier.weight(1f),
                            drawableId = R.drawable.vec_random,
                            text = "Random",
                            onClick = viewModel::onRandomAvatarPressed
                        )
                        ButtonIconComposable(
                            Modifier.weight(1f),
                            drawableId = R.drawable.vec_magic,
                            text = "Use AI",
                            isPremium = true,
                            onClick = {
                                navController.navigate(AiConversationRouteDestination.GenerateImage.route)
                            }
                        )
                    }
                }
            }

            item(key = "Button-Confirm") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f),
                    contentAlignment = Alignment.Center
                ) {
                    ButtonComposable(
                        title = when (viewModel.state) {
                            ImageGeneratorState.NotConnected -> stringResource(R.string.ai_conversation_signin_to_generate)
                            else -> stringResource(R.string.ai_conversation_generate)
                        },
                        theme = ButtonTheme.Pink(iconId = null),
                        onClick = onClick@{
                            if (viewModel.state == ImageGeneratorState.NotConnected) {
                                viewModel.signIn()
                                return@onClick
                            }

                            val nameValidationResult =
                                NameValidator.validate(nameInputTextViewModel.text.value)
                            if (nameValidationResult != ValidationResult.Success) {
                                nameInputTextViewModel.handle(nameValidationResult)
                                viewModel.alert.value = nameValidationResult.message
                                nameValidationResult.message?.let {
                                    viewModel.toast(it)
                                }
                                return@onClick
                            }

                            viewModel.onSubmit(
                                name = nameInputTextViewModel.text.value,
                                lastName = lastNameInputTextViewModel.text.value
                            )
                        }
                    )
                }
            }
        }
    }
}
package fr.purpletear.sutoko.screens.main.presentation.screens.community.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.purpletear.sutoko.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CommunitySearchBox(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isClearable: Boolean,
    textChanged: (String) -> Unit = {},
    onDoneAction: (String) -> Unit,
    onClear: () -> Unit
) {

    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .then(modifier)
            .clip(
                RoundedCornerShape(12.dp)
            )
            .fillMaxWidth()
            .background(Color.White.copy(0.1f))
            .height(48.dp)
    ) {
        BasicTextField(
            value = textState,
            onValueChange = {
                textState = it
                textChanged(it.text)
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onDoneAction(textState.text)
                }
            ),
            textStyle = TextStyle(
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
            ),
            cursorBrush = SolidColor(Color.White),
            singleLine = true,
            modifier = Modifier
                .background(Color.Transparent)
                .align(Alignment.CenterStart),
            decorationBox = {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Icon(
                        modifier = Modifier.offset(y = 2.dp),
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(0.5f)
                    )

                    Box {
                        if (textState.text.isEmpty()) {
                            Text(
                                stringResource(id = R.string.sutoko_community_toolbox_search_hint),
                                color = Color.White.copy(0.5f),
                                fontSize = 13.sp
                            )
                        }
                        it()
                    }
                    Spacer(modifier.weight(1f))
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp),

                            color = Color.White.copy(0.5f),

                            strokeWidth = 2.dp
                        )
                    } else if (isClearable) {
                        CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                        // TODO
                        /* IconButton(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp),
                                onClick = {
                                    textState = TextFieldValue("")
                                    onClear()
                                    focusManager.clearFocus()
                                }) {
                                Icon(
                                    imageVector = Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    tint = Color.White.copy(0.5f)
                                )
                            }  */
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun Button() {
    // Rounded
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(
                RoundedCornerShape(12.dp)
            )
            .background(Color(0xFFff1d67))
    ) {
        Image(
            painter = painterResource(id = R.drawable.sutoko_ic_ranking),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp)
        )
    }
}
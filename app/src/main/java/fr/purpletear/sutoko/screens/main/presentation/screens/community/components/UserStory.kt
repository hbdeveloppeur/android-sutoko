package fr.purpletear.sutoko.screens.main.presentation.screens.community.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.purpletear.smsgame.activities.smsgame.objects.Story
import fr.purpletear.sutoko.R
import com.example.sharedelements.theme.SutokoTypography

@Composable
fun UserStory(story: Story, onClick: (Story) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.Gray)
            .clickable(onClick = { onClick(story) })
    ) {
        // Image background crop center
        Image(
            painter = painterResource(id = R.drawable.image_bg_test),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Filter max size white with opacity
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x57000000))
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = story.title,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                style = SutokoTypography.h2.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = when (story.theme) {
                        2 -> stringResource(id = R.string.sutoko_theme_love)
                        3 -> stringResource(id = R.string.sutoko_theme_drama)
                        else -> stringResource(id = R.string.sutoko_theme_horror)
                    },
                    textAlign = TextAlign.Center,
                    color = Color(0xFFD54866),
                    fontSize = 12.sp,
                    style = SutokoTypography.h2.copy(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )


                Text(
                    text = stringResource(id = R.string.characteristic_dice),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF646167),
                    fontSize = 12.sp,
                    style = SutokoTypography.h2.copy(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }

            // Align vertical center
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {


                Text(
                    text = story.authorCachedName,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    style = SutokoTypography.h2.copy(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )

                Spacer(modifier = Modifier.weight(1f))


                Text(
                    text = "${story.likes}",
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    style = SutokoTypography.h2.copy(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )

                // Like icon R.drawable.ic_heart
                Image(
                    painter = painterResource(id = R.drawable.ic_heart),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


@Composable
private fun ProfilePicture() {

}
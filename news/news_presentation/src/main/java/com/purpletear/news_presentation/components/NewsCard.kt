package com.purpletear.news_presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sharedelements.theme.Boldonse
import com.example.sharedelements.theme.Poppins
import com.purpletear.core.presentation.components.AnimatedNewsGradient
import com.purpletear.news_presentation.R
import com.purpletear.news_presentation.viewmodels.NewsCardViewModel
import com.purpletear.sutoko.core.domain.appaction.ActionName
import com.purpletear.sutoko.core.domain.date.RelativeDateFormatter
import com.purpletear.sutoko.news.model.News

@Composable
fun NewsCard(
    modifier: Modifier = Modifier,
    news: News,
    onClick: (News) -> Unit,
    viewModel: NewsCardViewModel = hiltViewModel()
) {

    val shape = RoundedCornerShape(8.dp)
    ConstraintLayout(
        modifier
            .aspectRatio(1f / 0.7f)
            .heightIn(min = 300.dp)
            .clip(shape)
            .border(1.dp, Color.White.copy(0.11f), shape)
    ) {
        val (image, gradient, text, button, dateRef) = createRefs()

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(viewModel.getImageLink(news))
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(image) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.05f
                    scaleY = 1.05f
                },
            contentScale = ContentScale.Crop
        )

        AnimatedNewsGradient(
            modifier = Modifier
                .constrainAs(gradient) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .fillMaxSize()
        )

        NewsCardText(
            modifier = Modifier
                .constrainAs(text) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    // This is the magic
                    this.verticalBias = 0.825f
                    this.horizontalBias = 0.1f
                },
            title = news.metadata.title,
            subtitle = news.metadata.subtitle,
        )

        ButtonText(
            modifier = Modifier
                .constrainAs(button) {
                    top.linkTo(text.top)
                    bottom.linkTo(text.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)

                    this.horizontalBias = 0.92f
                },
            news = news,
            onClick = {
                onClick(news)
            },
        )

        news.releaseDateAndroid?.let { date ->
            NewsCardDate(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .constrainAs(dateRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)

                        this.horizontalBias = 0.1f
                        this.verticalBias = 0.1f
                    },
                date = date
            )
        }

    }
}

@Composable
private fun ButtonText(modifier: Modifier = Modifier, news: News, onClick: () -> Unit) {
    val buttonTextRes: Int? = when (news.action?.name) {
        ActionName.OpenLink -> R.string.news_button_open
        ActionName.OpenGame -> R.string.news_button_play
        ActionName.OpenPage -> R.string.news_button_open
        null -> null
    }
    if (buttonTextRes == null) {
        return
    }
    Box(
        modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF8F8F8))
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),

        ) {
        Text(
            text = stringResource(id = buttonTextRes),
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun NewsCardText(modifier: Modifier = Modifier, title: String, subtitle: String) {

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontFamily = Boldonse,
            color = Color.White,
            fontSize = 12.sp
        )
        Text(
            text = subtitle,
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun NewsCardDate(modifier: Modifier = Modifier, date: Long) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF8F8F8))
            .then(modifier)
            .height(28.dp)
            .wrapContentWidth()
    ) {
        Box(
            Modifier
                .size(28.dp)
                .background(Color(0xFFFF1A72)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.news_calendar),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )

        }

        Box(
            Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            val context = LocalContext.current
            Text(
                text = RelativeDateFormatter.formatNewsDate(context, date),
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                fontSize = 10.sp
            )
        }
    }
}



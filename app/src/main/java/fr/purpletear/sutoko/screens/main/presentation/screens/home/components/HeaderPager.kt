package fr.purpletear.sutoko.screens.main.presentation.screens.home.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.purpletear.news_presentation.components.NewsCard
import com.purpletear.sutoko.core.domain.appaction.AppAction
import com.purpletear.sutoko.news.model.News
import fr.purpletear.sutoko.presentation.util.LogCompositions

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HeaderPager(
    news: List<News>,
    initialPage: Int,
    onNewsPressed: (AppAction) -> Unit,
    onPageChanged: (Int) -> Unit
) {
    LogCompositions(name = "News", level = 3)
    val pagerState = rememberPagerState(initialPage = initialPage)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { pageIndex ->
            onPageChanged(pageIndex)
        }
    }

    HorizontalPager(
        modifier = Modifier, count = news.size,
        contentPadding = PaddingValues(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { index ->

        NewsCard(
            news = news[index],
            onClick = { news ->
                news.action?.let {
                    onNewsPressed(it)
                }
            }
        )
    }
}

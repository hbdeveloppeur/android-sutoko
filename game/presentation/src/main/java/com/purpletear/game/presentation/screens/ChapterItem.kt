package com.purpletear.game.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.model.ChapterState
import com.purpletear.sutoko.game.model.Chapter

@Composable
fun ChapterItem(
    chapter: Chapter,
    chapterState: ChapterState,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (chapterState) {
                ChapterState.Current -> Color(0x44FFFFFF)
                else -> Color(0x22FFFFFF)
            }
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (chapterState) {
                    ChapterState.Locked -> {

                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.game_chapter_locked),
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        // Display chapter number with lock icon
                        Text(
                            text = stringResource(R.string.game_chapter_number, chapter.number),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    else -> {
                        // Display chapter number and title
                        Text(
                            text = stringResource(R.string.game_chapter_number_title, chapter.number, chapter.title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (chapterState != ChapterState.Locked) {
                Text(
                    text = chapter.description ?: "",
                    style = MaterialTheme.typography.labelMedium.copy(
                        lineHeight = 20.sp
                    ),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chapterState != ChapterState.Locked) {
                when (chapterState) {
                    ChapterState.Played -> {
                        Box(
                            Modifier
                                .background(Color(0xFF101313), shape = CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable {
                                    onClick()
                                }
                        ) {
                            Text(
                                stringResource(R.string.game_chapter_return),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    ChapterState.Current -> {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50), shape = CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.game_chapter_current),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

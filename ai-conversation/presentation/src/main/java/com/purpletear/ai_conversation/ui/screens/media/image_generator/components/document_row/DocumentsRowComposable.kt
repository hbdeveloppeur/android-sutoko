package com.purpletear.ai_conversation.ui.screens.media.image_generator.components.document_row

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.purpletear.ai_conversation.domain.model.Document
import com.purpletear.ai_conversation.presentation.R
import com.purpletear.ai_conversation.ui.common.utils.getDocumentLastImageUrl


private val size = 56.dp

@Composable
internal fun DocumentsRowComposable(
    modifier: Modifier = Modifier,
    viewModel: DocumentsRowViewModel = hiltViewModel(),
) {
    val scrollState = rememberScrollState()
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AddButtonComposable(onClick = {
            viewModel.onClickNewDocument()
        })
        viewModel.documents.value.sortedByDescending { it.createdAt }.forEach { document ->
            DocumentComposable(
                document = document,
                isSelected = viewModel.selectedDocumentId.value == document.serial,
                onClick = { viewModel.onClickDocument(document) })
        }
    }
}

@Composable
private fun DocumentComposable(document: Document, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .size(size)
            .border(1.dp, Color.White.copy(if (isSelected) 0.3f else 0.1f), shape)
            .clip(shape)
            .background(Color(0xFF040617), shape)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isSelected) 1f else 0.25f),
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    getDocumentLastImageUrl(document)
                        ?: "https://data.sutoko.app/resources/sutoko-ai/image/background_waiting_screen.jpg"
                )
                .crossfade(true).build(),
            contentDescription = null, contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun AddButtonComposable(onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .size(size)
            .border(1.dp, Color.White.copy(0.2f), shape)
            .clip(shape)
            .background(Color(0xFF040617), shape)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        val vector = ImageVector.vectorResource(id = R.drawable.vec_add_box)
        val painter = rememberVectorPainter(image = vector)

        Image(
            painter = painter,
            contentDescription = "Add Box",
            modifier = Modifier.size(14.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFD9E3F8))
        )
    }
}
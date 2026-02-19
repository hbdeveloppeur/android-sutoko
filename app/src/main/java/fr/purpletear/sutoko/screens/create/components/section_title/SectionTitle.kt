package fr.purpletear.sutoko.screens.create.components.section_title

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharedelements.theme.Poppins

@Composable
internal fun SectionTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        modifier = modifier
            .padding(horizontal = 16.dp)
    )
}

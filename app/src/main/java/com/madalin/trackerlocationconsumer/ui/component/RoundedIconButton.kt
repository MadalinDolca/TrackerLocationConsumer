package com.madalin.trackerlocationconsumer.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RoundedIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: Dp = 45.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = null
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(3.dp),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size)
                .padding(8.dp),
            tint = Color.White
        )
    }
}
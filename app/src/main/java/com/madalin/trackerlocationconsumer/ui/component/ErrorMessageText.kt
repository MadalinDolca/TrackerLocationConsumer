package com.madalin.trackerlocationconsumer.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ErrorMessageText(errorMessage: String?) {
    // if an error is thrown, show a text
    errorMessage?.let {
        Text(
            text = it,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Red,
            fontWeight = FontWeight.Bold
        )
    }
}
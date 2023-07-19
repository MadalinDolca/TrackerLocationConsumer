package com.madalin.trackerlocationconsumer.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun PasswordResetScreen(navigator: DestinationsNavigator) {
    Text(text = "password reset")
}
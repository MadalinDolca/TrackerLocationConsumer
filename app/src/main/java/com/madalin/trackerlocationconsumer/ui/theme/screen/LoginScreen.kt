package com.madalin.trackerlocationconsumer.ui.theme.screen

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.madalin.trackerlocationconsumer.viewmodel.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madalin.trackerlocationconsumer.entity.LoginAction

@Composable
fun LoginScreen(loginViewModel: LoginViewModel = viewModel()) {
    val viewState by loginViewModel.viewState.collectAsState()

    Button(onClick = {
        loginViewModel.handleAction(
            LoginAction.DoLogin(
                username = viewState.username,
                password = viewState.password
            )
        )
    }) {

    }
}
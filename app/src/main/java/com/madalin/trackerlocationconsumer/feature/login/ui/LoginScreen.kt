package com.madalin.trackerlocationconsumer.feature.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.madalin.trackerlocationconsumer.R
import com.madalin.trackerlocationconsumer.feature.login.LoginAction
import com.madalin.trackerlocationconsumer.feature.login.LoginViewModel
import com.madalin.trackerlocationconsumer.ui.theme.TrackerLocationConsumerTheme
import org.koin.androidx.compose.getViewModel

@Composable
fun LoginScreen(loginViewModel: LoginViewModel = getViewModel()) { // get the view model from koin (same as koinViewModel())
    val viewState by loginViewModel.viewState.collectAsState() // obtains the state of the view from the view model

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            // calls the associated view model's handleAction() with the given action
            loginViewModel.handleAction(
                LoginAction.DoLogin(
                    username = viewState.username,
                    password = viewState.password
                )
            )
        }) {
            Text(text = stringResource(R.string.login))
        }
    }
}

@Preview
@Composable
fun PreviewLoginScreen() {
    TrackerLocationConsumerTheme {
        LoginScreen()
    }
}
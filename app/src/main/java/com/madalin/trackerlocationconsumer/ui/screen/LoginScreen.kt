package com.madalin.trackerlocationconsumer.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.madalin.trackerlocationconsumer.R
import com.madalin.trackerlocationconsumer.entity.LoginAction
import com.madalin.trackerlocationconsumer.feature.auth.LoginScreenAction
import com.madalin.trackerlocationconsumer.feature.auth.LoginViewModel
import com.madalin.trackerlocationconsumer.ui.component.ErrorMessageText
import com.madalin.trackerlocationconsumer.ui.screen.destinations.SignUpScreenDestination
import com.madalin.trackerlocationconsumer.ui.screen.destinations.TrackerScreenDestination
import com.madalin.trackerlocationconsumer.ui.theme.Purple40
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.getViewModel

@RootNavGraph(start = true) // start destination
@Destination
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = getViewModel(), // get the view model from koin (same as koinViewModel())
    navigator: DestinationsNavigator
) {
    val viewState by loginViewModel.viewState.collectAsState() // obtains the state of the view from the view model

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login),
            modifier = Modifier.padding(bottom = 10.dp),
            fontSize = 40.sp
        )

        ErrorMessageText(errorMessage = viewState.errorMessage)
        EmailTextField(
            handleAction = { email -> loginViewModel.handleLoginScreenAction(LoginScreenAction.UpdateEmailTextField(email)) },
            email = viewState.email
        )
        PasswordTextField(
            handleAction = { password -> loginViewModel.handleLoginScreenAction(LoginScreenAction.UpdatePasswordTextField(password)) },
            password = viewState.password
        )
        LoginButton(
            login = { loginViewModel.handleAction(LoginAction.DoLogin(email = viewState.email, password = viewState.password)) },
            isLoggedIn = viewState.isLoggedIn,
            navToTracker = { navigator.navigate(TrackerScreenDestination) }
        )
    }

    SignUpRedirectText(navToSignUp = { navigator.navigate(SignUpScreenDestination) })
}

@Composable
private fun EmailTextField(handleAction: (String) -> Unit, email: String) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = email,
        onValueChange = { handleAction(it) },
        placeholder = { Text(text = stringResource(R.string.email)) },
        singleLine = true
    )
}

@Composable
private fun PasswordTextField(handleAction: (String) -> Unit, password: String) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = password,
        onValueChange = { handleAction(it) },
        placeholder = { Text(text = stringResource(id = R.string.password)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true
    )
}

@Composable
fun LoginButton(login: () -> Unit, isLoggedIn: Boolean, navToTracker: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp),
        onClick = { login() }
    ) {
        Text(
            text = stringResource(R.string.login),
            fontSize = 20.sp
        )
    }

    // observe the isLoggedIn state and navigate to the Tracker screen when it becomes true
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navToTracker()
        }
    }
}

@Composable
fun SignUpRedirectText(navToSignUp: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        ClickableText(
            text = AnnotatedString(stringResource(R.string.sign_up)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            style = TextStyle(
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline,
                color = Purple40
            ),
            onClick = { navToSignUp() } // navigate to Sign Up
        )
    }
}

/*@Preview
@Composable
fun PreviewLoginScreen() {
    TrackerLocationConsumerTheme {
        LoginScreen()
    }
}*/
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.madalin.trackerlocationconsumer.R
import com.madalin.trackerlocationconsumer.entity.LoginAction
import com.madalin.trackerlocationconsumer.feature.auth.LoginScreenAction
import com.madalin.trackerlocationconsumer.feature.auth.LoginViewModel
import com.madalin.trackerlocationconsumer.feature.auth.LoginViewState
import com.madalin.trackerlocationconsumer.navigation.Routes
import com.madalin.trackerlocationconsumer.ui.component.ErrorMessageText
import com.madalin.trackerlocationconsumer.ui.theme.Purple40
import org.koin.androidx.compose.getViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = getViewModel(), // get the view model from koin (same as koinViewModel())
    navController: NavHostController
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
        EmailTextField(loginViewModel = loginViewModel, email = viewState.email)
        PasswordTextField(loginViewModel = loginViewModel, password = viewState.password)
        LoginButton(loginViewModel = loginViewModel, viewState = viewState, navController = navController)
    }

    SignUpRedirectText(navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailTextField(loginViewModel: LoginViewModel, email: String) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = email,
        onValueChange = { loginViewModel.handleLoginScreenAction(LoginScreenAction.UpdateEmailTextField(it)) },
        placeholder = { Text(text = stringResource(R.string.email)) },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(loginViewModel: LoginViewModel, password: String) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = password,
        onValueChange = { loginViewModel.handleLoginScreenAction(LoginScreenAction.UpdatePasswordTextField(it)) },
        placeholder = { Text(text = stringResource(id = R.string.password)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true
    )
}

@Composable
fun LoginButton(loginViewModel: LoginViewModel, viewState: LoginViewState, navController: NavHostController) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp),
        onClick = {
            // calls the associated view model's handleAction() with the given action
            loginViewModel.handleAction(
                LoginAction.DoLogin(
                    email = viewState.email,
                    password = viewState.password
                )
            )
        }
    ) {
        Text(
            text = stringResource(R.string.login),
            fontSize = 20.sp
        )
    }

    // observe the isLoggedIn state and navigate to the Tracker screen when it becomes true
    LaunchedEffect(viewState.isLoggedIn) {
        if (viewState.isLoggedIn) {
            navController.navigate(Routes.Tracker.route)
        }
    }
}

@Composable
fun SignUpRedirectText(navController: NavHostController) {
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
            onClick = { navController.navigate(Routes.SignUp.route) } // navigate to Sign Up
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
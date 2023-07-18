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
import androidx.navigation.NavHostController
import com.madalin.trackerlocationconsumer.R
import com.madalin.trackerlocationconsumer.feature.auth.SignUpScreenAction
import com.madalin.trackerlocationconsumer.feature.auth.SignUpViewModel
import com.madalin.trackerlocationconsumer.navigation.Routes
import com.madalin.trackerlocationconsumer.ui.component.ErrorMessageText
import com.madalin.trackerlocationconsumer.ui.theme.Purple40
import org.koin.androidx.compose.getViewModel

@Composable
fun SignUpScreen(
    signUpViewModel: SignUpViewModel = getViewModel(),
    navController: NavHostController
) {
    val viewState by signUpViewModel.viewState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.sign_up),
            modifier = Modifier.padding(bottom = 10.dp),
            fontSize = 40.sp
        )

        ErrorMessageText(errorMessage = viewState.errorMessage)
        UsernameTextField(
            handleAction = { username -> signUpViewModel.handleAction(SignUpScreenAction.UpdateUsernameTextField(username)) },
            username = viewState.username
        )
        EmailTextField(
            handleAction = { email -> signUpViewModel.handleAction(SignUpScreenAction.UpdateEmailTextField(email)) },
            email = viewState.email
        )
        PasswordTextField(
            handleAction = { password -> signUpViewModel.handleAction(SignUpScreenAction.UpdatePasswordTextField(password)) },
            password = viewState.password
        )
        SignUpButton(
            createAccount = { signUpViewModel.handleAction(SignUpScreenAction.CreateAccount(viewState.username, viewState.email, viewState.password)) },
            hasRegistered = viewState.hasRegistered,
            navToLogin = { navController.navigate(Routes.Login.route) }
        )
    }

    LoginRedirectText(navToLogin = { navController.navigate(Routes.Login.route) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UsernameTextField(handleAction: (String) -> Unit, username: String) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = username,
        onValueChange = { handleAction(it) },
        placeholder = { Text(text = stringResource(id = R.string.username)) },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailTextField(handleAction: (String) -> Unit, email: String) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = email,
        onValueChange = { handleAction(it) },
        placeholder = { Text(text = stringResource(id = R.string.email)) },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
fun SignUpButton(createAccount: () -> Unit, hasRegistered: Boolean, navToLogin: () -> Unit) {
    Button(modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp),
        onClick = { createAccount() }) {
        Text(
            text = stringResource(id = R.string.sign_up),
            fontSize = 20.sp
        )
    }

    // observe the hasRegistered state and navigate to the Login screen when it becomes true
    LaunchedEffect(hasRegistered) {
        if (hasRegistered) {
            navToLogin()
        }
    }
}

@Composable
fun LoginRedirectText(navToLogin: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        ClickableText(
            text = AnnotatedString(stringResource(R.string.login)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            style = TextStyle(
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline,
                color = Purple40
            ),
            onClick = { navToLogin() }
        )
    }
}
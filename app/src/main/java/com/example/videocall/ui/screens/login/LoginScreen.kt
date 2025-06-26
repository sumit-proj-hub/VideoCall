package com.example.videocall.ui.screens.login

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.videocall.ui.components.FormTextField
import com.example.videocall.ui.theme.VideoCallTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModel = hiltViewModel(),
    onSignUp: () -> Unit,
    onSuccessfulLogin: () -> Unit,
) {
    val uiState = viewModel.uiState
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .imePadding()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = it)
                .verticalScroll(rememberScrollState())
                .safeContentPadding()
        ) {
            LoginCard(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLogin = {
                    viewModel.onLogin(
                        onSuccessfulLogin = onSuccessfulLogin,
                        showSnackBar = { message ->
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(message)
                            }
                        }
                    )
                },
                onSignUp = onSignUp
            )
        }
    }
}

@Composable
private fun LoginCard(
    uiState: LoginUiState = LoginUiState(),
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onLogin: () -> Unit = {},
    onSignUp: () -> Unit = {},
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Login",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(16.dp))

            FormTextField(
                text = uiState.email,
                label = "Email",
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                error = uiState.emailError,
                onValueChanged = onEmailChange,
                maxLength = 20
            )

            FormTextField(
                text = uiState.password,
                label = "Password",
                placeholder = "Password",
                leadingIcon = Icons.Default.Key,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                error = uiState.passwordError,
                onValueChanged = onPasswordChange,
                maxLength = 20
            )

            Spacer(Modifier.height(16.dp))

            Button(onClick = onLogin, shape = MaterialTheme.shapes.small) {
                Text(text = "LOGIN")
            }

            Text("Or SignUp From Here", modifier = Modifier.padding(8.dp))
            OutlinedButton(onClick = onSignUp, shape = MaterialTheme.shapes.small) {
                Text(text = "SIGN UP")
            }
        }
    }
}

@Composable
@Preview(uiMode = UI_MODE_NIGHT_YES)
private fun LoginCardPreview() {
    VideoCallTheme {
        LoginCard()
    }
}
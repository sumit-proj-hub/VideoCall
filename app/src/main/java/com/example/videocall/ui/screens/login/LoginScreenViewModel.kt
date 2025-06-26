package com.example.videocall.ui.screens.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videocall.data.local.UserProfilePreference
import com.example.videocall.data.remote.Api
import com.example.videocall.data.remote.dto.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val api: Api,
    private val userProfilePreference: UserProfilePreference,
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, emailError = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, passwordError = null)
    }

    private fun validateInput(): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()

        if (!emailRegex.matches(uiState.email)) {
            uiState = uiState.copy(emailError = "Invalid email")
            return false
        }

        if (uiState.password.isEmpty()) {
            uiState = uiState.copy(passwordError = "Required")
            return false
        }

        return true
    }

    fun onLogin(onSuccessfulLogin: () -> Unit, showSnackBar: (String) -> Unit) {
        if (!validateInput()) return
        viewModelScope.launch {
            try {
                val response = api.login(
                    LoginRequest(uiState.email, uiState.password)
                )
                if (response.code() != 200) {
                    uiState = uiState.copy(
                        emailError = "Invalid Credentials",
                        passwordError = "Invalid Credentials"
                    )
                    return@launch
                }
                val tokenResponse = response.body()!!
                userProfilePreference.saveUserProfile(tokenResponse.toUserProfile())
                onSuccessfulLogin()
            } catch (e: Exception) {
                showSnackBar("Network Error")
                Log.e("LoginScreenViewModel", e.stackTraceToString())
            }
        }
    }
}
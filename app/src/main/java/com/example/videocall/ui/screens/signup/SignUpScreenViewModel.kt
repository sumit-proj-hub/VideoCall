package com.example.videocall.ui.screens.signup

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videocall.data.local.UserProfilePreference
import com.example.videocall.data.remote.Api
import com.example.videocall.data.remote.dto.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpScreenViewModel @Inject constructor(
    private val api: Api,
    private val userProfilePreference: UserProfilePreference,
) : ViewModel() {
    var uiState by mutableStateOf(SignUpUiState())
        private set

    fun onNameChange(name: String) {
        uiState = uiState.copy(name = name, nameError = null)
    }

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, emailError = null)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, passwordError = null)
    }

    private fun validateInput(): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()

        if (uiState.name.isEmpty()) {
            uiState = uiState.copy(nameError = "Required")
            return false
        }

        if (!emailRegex.matches(uiState.email)) {
            uiState = uiState.copy(emailError = "Invalid email")
            return false
        }

        if (uiState.password.length < 8) {
            uiState = uiState.copy(passwordError = "Must be at least 8 characters")
            return false
        }

        return true
    }

    fun onSignUp(onSuccessfulSignUp: () -> Unit, showSnackBar: (String) -> Unit) {
        if (!validateInput()) return
        viewModelScope.launch {
            try {
                val response = api.register(
                    RegisterRequest(uiState.name, uiState.email, uiState.password)
                )
                if (response.code() != 200) {
                    uiState = uiState.copy(emailError = "Email Already Registered")
                    return@launch
                }
                val tokenResponse = response.body()!!
                userProfilePreference.saveUserProfile(
                    tokenResponse.toUserProfile()
                )
                onSuccessfulSignUp()
            } catch (e: Exception) {
                showSnackBar("Network Error")
                Log.e("SignUpViewModel", e.stackTraceToString())
            }
        }
    }
}
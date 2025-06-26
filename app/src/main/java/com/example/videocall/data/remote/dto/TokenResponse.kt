package com.example.videocall.data.remote.dto

import android.util.Base64
import com.example.videocall.data.model.UserProfile
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
data class TokenResponse(
    val success: Boolean,
    val token: String,
) {
    fun toUserProfile(): UserProfile {
        val jsonPayload = token.split(".")[1]
        val decodedPayload = Base64.decode(jsonPayload, Base64.DEFAULT).decodeToString()
        val jsonObject = JSONObject(decodedPayload)
        val name = jsonObject.getString("name")
        val email = jsonObject.getString("email")
        return UserProfile(name, email, token)
    }
}

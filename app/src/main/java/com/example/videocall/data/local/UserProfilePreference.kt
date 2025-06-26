package com.example.videocall.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.videocall.Constants
import com.example.videocall.data.model.UserProfile
import com.example.videocall.di.AppModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(Constants.USER_PREFERENCE_NAME)

class UserProfilePreference @Inject constructor(
    @AppModule.UserDataStore private val dataStore: DataStore<Preferences>,
) {
    private companion object {
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val JWT_TOKEN_KEY = stringPreferencesKey("user_token")
    }

    val userProfile: Flow<UserProfile> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e("UserProfilePreference", "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            UserProfile(
                name = preferences[USER_NAME_KEY] ?: "",
                email = preferences[USER_EMAIL_KEY] ?: "",
                token = preferences[JWT_TOKEN_KEY] ?: ""
            )
        }

    suspend fun saveUserProfile(userProfile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = userProfile.name
            preferences[USER_EMAIL_KEY] = userProfile.email
            preferences[JWT_TOKEN_KEY] = userProfile.token
        }
    }
}
package com.example.videocall.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.videocall.Constants.BASE_URL
import com.example.videocall.data.local.RecentRoomsPreference
import com.example.videocall.data.local.UserProfilePreference
import com.example.videocall.data.local.recentRoomDataStore
import com.example.videocall.data.local.userDataStore
import com.example.videocall.data.remote.Api
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.webrtc.EglBase
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class UserDataStore

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RecentRoomDataStore

    @Provides
    @Singleton
    fun providesApi(): Api {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(Api::class.java)
    }

    @Provides
    @Singleton
    @UserDataStore
    fun providesDataStore(@ApplicationContext context: Context) = context.userDataStore

    @Provides
    @Singleton
    @RecentRoomDataStore
    fun providesRecentRoomDataStore(@ApplicationContext context: Context) =
        context.recentRoomDataStore

    @Provides
    @Singleton
    fun providesUserProfilePreference(
        @UserDataStore dataStore: DataStore<Preferences>,
    ): UserProfilePreference {
        return UserProfilePreference(dataStore)
    }

    @Provides
    @Singleton
    fun providesRecentRoomsPreference(
        @RecentRoomDataStore dataStore: DataStore<Preferences>,
    ): RecentRoomsPreference {
        return RecentRoomsPreference(dataStore)
    }

    @Provides
    @Singleton
    fun providesEglBase(): EglBase = EglBase.create()
}
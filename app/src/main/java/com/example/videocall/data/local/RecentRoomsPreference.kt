package com.example.videocall.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.videocall.di.AppModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

val Context.recentRoomDataStore by preferencesDataStore(name = "recent_rooms")

class RecentRoomsPreference @Inject constructor(
    @AppModule.RecentRoomDataStore private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val RECENT_ROOMS_KEY = stringPreferencesKey("recent_rooms")
    }

    val recentRooms: Flow<List<RecentRoom>> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e("RecentRoomsPreference", "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { prefs ->
            val jsonString = prefs[RECENT_ROOMS_KEY] ?: return@map emptyList()
            try {
                Json.decodeFromString(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun addRoom(room: RecentRoom) {
        val currentList = recentRooms.first().toMutableList()
        currentList.removeAll { it.id == room.id }
        currentList.add(0, room)
        val updatedList = currentList.take(5)
        saveRooms(updatedList)
    }

    private suspend fun saveRooms(rooms: List<RecentRoom>) {
        dataStore.edit { prefs ->
            prefs[RECENT_ROOMS_KEY] = Json.encodeToString(rooms)
        }
    }
}

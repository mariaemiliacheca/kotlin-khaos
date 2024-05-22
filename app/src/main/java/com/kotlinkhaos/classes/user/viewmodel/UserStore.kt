package com.kotlinkhaos.classes.user.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kotlinkhaos.classes.user.UserType
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_details_preferences")

data class StoredUserDetails(val userCourseId: String, val userType: UserType)

class UserStore(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val USER_COURSE_ID_KEY = stringPreferencesKey("user_course_id")
        val USER_TYPE_KEY = stringPreferencesKey("user_type")
    }

    suspend fun saveUserDetails(userDetails: StoredUserDetails) {
        dataStore.edit { preferences ->
            preferences[USER_COURSE_ID_KEY] = userDetails.userCourseId
            preferences[USER_TYPE_KEY] = userDetails.userType.name
        }
    }

    suspend fun loadUserDetails(): StoredUserDetails? {
        val preferences = dataStore.data.first()
        val userTypeString = preferences[USER_TYPE_KEY] ?: return null
        val userCourseId = preferences[USER_COURSE_ID_KEY] ?: return null

        val userType = try {
            UserType.valueOf(userTypeString)
        } catch (e: IllegalArgumentException) {
            UserType.NONE
        }

        return StoredUserDetails(userCourseId, userType)
    }

    suspend fun clearUserDetails() {
        dataStore.edit { preferences ->
            preferences.remove(USER_TYPE_KEY)
            preferences.remove(USER_COURSE_ID_KEY)
        }
    }
}

package com.example.guitarapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SessionManager {
    private const val PREFS_NAME = "guitar_app_prefs"
    private const val SESSION_ID_KEY = "session_id"
    private const val USER_ID = "user_id"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSessionId(context: Context, sessionId: String) {
        getPrefs(context).edit() { putString(SESSION_ID_KEY, sessionId) }
    }

    fun getSessionId(context: Context): String? {
        return getPrefs(context).getString(SESSION_ID_KEY, null)
    }

    fun saveUserId(context: Context, userId: Int) {
        getPrefs(context).edit { putInt(USER_ID, userId) }
    }

    fun getUserId(context: Context): Int? {
        return getPrefs(context).getInt(USER_ID, 1)
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit() { remove(SESSION_ID_KEY); remove(USER_ID)}
    }
}
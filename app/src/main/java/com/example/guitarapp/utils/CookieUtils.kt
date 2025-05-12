package com.example.guitarapp.utils

import retrofit2.Response


object CookieUtils {

    fun <T> extractSessionId(response: Response<T>): String? {
        val headers = response.headers()

        val cookieSession = headers.values("Set-Cookie")

        return cookieSession.firstOrNull { it.startsWith("JSESSIONID") }
            ?.substringBefore(";")
    }

    fun <T> extractUserId(response: Response<T>): String? {
        val headers = response.headers()
        val cookieLocation = headers.values("Location")

        return cookieLocation.firstOrNull { it.contains("users/") }
            ?.substringAfter("users/")
    }
}
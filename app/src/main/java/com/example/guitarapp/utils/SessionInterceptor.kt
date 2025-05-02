package com.example.guitarapp.utils

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.lang.ref.WeakReference

class SessionInterceptor(context: Context) : Interceptor {

    private val contextRef = WeakReference(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val context = contextRef.get()
        val sessionId = context?.let { SessionManager.getSessionId(it) }

        val request = chain.request().newBuilder().apply {
            sessionId?.let {
                addHeader("Cookie", "$it;")
            }
        }.build()

        return chain.proceed(request)
    }
}
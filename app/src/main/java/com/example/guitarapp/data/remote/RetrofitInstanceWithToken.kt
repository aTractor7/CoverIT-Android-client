package com.example.guitarapp.data.remote

import android.content.Context
import com.example.guitarapp.data.model.CommentCreate
import com.example.guitarapp.utils.Constants
import com.example.guitarapp.utils.SessionInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstanceWithToken {

    private var client: OkHttpClient? = null
    private var retrofit: Retrofit? = null

    fun init(context: Context) {
        client = OkHttpClient.Builder()
            .addInterceptor(SessionInterceptor(context))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client!!)
            .build()
    }

    val userApi: UserApi by lazy {
        retrofit!!.create(UserApi::class.java)
    }

    val songTutorialApi: SongTutorialApi by lazy {
        retrofit!!.create(SongTutorialApi::class.java)
    }

    val commentApi: CommentApi by lazy {
        retrofit!!.create(CommentApi::class.java)
    }
}
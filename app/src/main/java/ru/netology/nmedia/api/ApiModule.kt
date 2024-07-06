package ru.netology.nmedia.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {
    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
    }

    @Provides
    @Singleton
    fun provideRetrofit(appAuth: AppAuth): Retrofit = Retrofit.Builder()
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    if (BuildConfig.DEBUG) {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                })
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.run {
                            val token = appAuth.state.value?.token
                            if (token != null) {
                                request().newBuilder().addHeader("Authorization", token)
                                    .build()
                            } else {
                                request()
                            }
                        })
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ServiceAPI = retrofit.create()

}
//package ru.netology.nmedia.di
//
//import android.content.Context
//import androidx.room.Room
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.create
//import ru.netology.nmedia.BuildConfig
//import ru.netology.nmedia.api.ServiceAPI
//import ru.netology.nmedia.auth.AppAuth
//import ru.netology.nmedia.db.AppDB
//import ru.netology.nmedia.repository.PostRepository
//import ru.netology.nmedia.repository.PostRepositoryImpl
//import java.util.concurrent.TimeUnit
//
//class DependencyContainer(private val context: Context) {
//
//    companion object {
//        private const val BASE_URL = BuildConfig.BASE_URL
//
//        @Volatile
//        private var instance: DependencyContainer? = null
//
//        fun initApp(context: Context) {
//            instance = DependencyContainer(context)
//        }
//
//        fun getInstance(): DependencyContainer {
//            return instance!!
//        }
//    }
//
//    private val appBd = Room.databaseBuilder(context, AppDB::class.java, "app.db")
//        //.allowMainThreadQueries()
//        .build()
//    private val postDao = appBd.postDAO()
//
//    val appAuth = AppAuth(context)
//
//    private val retrofit = Retrofit.Builder()
//        .client(
//            OkHttpClient.Builder()
//                .addInterceptor(HttpLoggingInterceptor().apply {
//                    if (BuildConfig.DEBUG) {
//                        level = HttpLoggingInterceptor.Level.BODY
//                    }
//                })
//                .addInterceptor { chain ->
//                    chain.proceed(
//                        chain.run {
//                            val token = appAuth.state.value?.token
//                            if (token != null) {
//                                request().newBuilder().addHeader("Authorization", token)
//                                    .build()
//                            } else {
//                                request()
//                            }
//                        })
//                }
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .build()
//        )
//        .addConverterFactory(GsonConverterFactory.create())
//        .baseUrl(BASE_URL)
//        .build()
//
//    val apiService = retrofit.create<ServiceAPI>()
//
//    val repository: PostRepository = PostRepositoryImpl(postDao, apiService, appAuth)
//}
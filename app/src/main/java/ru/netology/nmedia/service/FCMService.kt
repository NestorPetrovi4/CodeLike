package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.viewmodel.AppActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var repository: PostRepository

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {

        message.data[action]?.let {
            when (AndroidUtils.enumValueOfOrNull<Action>(it)) {
                Action.LIKE -> handleLike(
                    gson.fromJson(
                        message.data[content],
                        Like::class.java
                    )
                )

                Action.NEWPOST -> handleNewPost(
                    gson.fromJson(
                        message.data[content],
                        Post::class.java
                    )
                )

                null -> handleNull(getString(R.string.app_err_message_received))
            }
            return@let
        }.also {
            message.data[content]?.let {
                visibleMessage(
                    gson.fromJson(
                        message.data[content],
                        Like::class.java
                    )
                )
            }
        }
    }

    override fun onNewToken(token: String) {
        val postViewModel = PostViewModel(repository, appAuth)
        postViewModel.addRepoValue("token", token)
        appAuth.sendPushToken(token)
    }

    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun visibleMessage(content: Like) {
        if (content.recipientId == null || appAuth.state.value?.id == content.recipientId) {
            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(
                    getString(
                        R.string.channel_remote,
                        content.content,
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            notify(notification)
        } else if (content.recipientId == 0 && appAuth.state.value?.id != 0) {
            appAuth.sendPushToken()
        } else if (content.recipientId != 0 && appAuth.state.value?.id != content.recipientId) {
            appAuth.sendPushToken()
        }
    }

    private fun handleNewPost(content: Post) {
        val intent = Intent(this, AppActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_new_post, content.author
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notify(notification)
    }

    private fun handleNull(content: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }
}

enum class Action {
    LIKE,
    NEWPOST,
}

data class Like(
    val userId: Int,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
    val recipientId: Int?,
    val content: String

)
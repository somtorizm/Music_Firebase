package com.vectorinc.musicfirebase.exoplayer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat.stopForeground
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.offline.DownloadService.startForeground
import com.google.android.exoplayer2.ui.DefaultMediaDescriptionAdapter
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.vectorinc.musicfirebase.R
import com.vectorinc.musicfirebase.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.vectorinc.musicfirebase.utils.Constants.NOTIFICATION_ID

@RequiresApi(Build.VERSION_CODES.N)
class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
) {

    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager =
            PlayerNotificationManager.Builder(context, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
                .setChannelImportance(NotificationManager.IMPORTANCE_DEFAULT)
                .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
                .setStopActionIconResourceId(R.drawable.ic_baseline_music_note_24)
                .build()
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(context).asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit

                })
            return null
        }

    }


}
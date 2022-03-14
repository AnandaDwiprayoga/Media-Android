package com.pasukanlangit.id.media

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
import java.io.IOException
import java.lang.ref.WeakReference

class MediaService : Service(), MediaPlayerCallback {
    private var mMediaPlayer: MediaPlayer? = null
    private var isReady: Boolean = false

    override fun onBind(intent: Intent): IBinder {
        return mMessenger.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if(action != null){
            when(action){
                ACTION_CREATE  -> if(mMediaPlayer == null){
                    init()
                }
                ACTION_DESTROY -> if(mMediaPlayer?.isPlaying == true){
                    stopSelf()
                }
                else -> {
                    init()
                }
            }
        }
        return flags
    }

    override fun onPlay() {
        if(!isReady){
            mMediaPlayer?.prepareAsync()
        }else{
            if(mMediaPlayer?.isPlaying == true){
                mMediaPlayer?.pause()
            }else{
                mMediaPlayer?.start()
                showNotif()
            }
        }
    }

    override fun onStop() {
        if(mMediaPlayer?.isPlaying == true || isReady){
            mMediaPlayer?.stop()
            isReady = false
            stopNotif()
        }
    }

    private fun init(){
        mMediaPlayer = MediaPlayer()
        val attribute = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        mMediaPlayer?.setAudioAttributes(attribute)
        val afd = applicationContext.resources.openRawResourceFd(R.raw.sword)
        try {
            mMediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        }catch(e: IOException){
            e.printStackTrace()
        }

        mMediaPlayer?.setOnPreparedListener {
            isReady = true
            mMediaPlayer?.start()
            showNotif()
        }

        mMediaPlayer?.setOnErrorListener { _, _, _ -> false }

    }

    private fun showNotif(){
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT

        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
            .setContentTitle("Now Playing...")
            .setContentText("Suara Pedang Pusaka-The beattle")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setTicker("TES3")
            .build()
        
        createChannel()
        
        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createChannel() {
        val mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(CHANNEL_DEFAULT_IMPORTANCE, "Battery", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setShowBadge(false)
        channel.setSound(null,null)
        mNotificationManager.createNotificationChannel(channel)
    }

    private fun stopNotif(){
        stopForeground(false)
    }


    private val mMessenger = Messenger(IncomingHandler(this))

    internal class IncomingHandler(playerCallback: MediaPlayerCallback): Handler(Looper.getMainLooper()){
        private val mediaPlayerCallbackWeakReference: WeakReference<MediaPlayerCallback> = WeakReference(playerCallback)

        override fun handleMessage(msg: Message) {
            when(msg.what){
                PLAY -> mediaPlayerCallbackWeakReference.get()?.onPlay()
                STOP -> mediaPlayerCallbackWeakReference.get()?.onStop()
                else -> super.handleMessage(msg)
            }
        }
    }

    companion object {
        const val ACTION_CREATE = "com.pasukanlangit.id.media.create"
        const val ACTION_DESTROY = "com.pasukanlangit.id.media.destroy"
        const val TAG = "MediaService"
        const val PLAY = 0
        const val STOP = 1
        const val CHANNEL_DEFAULT_IMPORTANCE = "Channel_Test"
        const val ONGOING_NOTIFICATION_ID = 1
    }
}
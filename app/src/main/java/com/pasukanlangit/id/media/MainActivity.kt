package com.pasukanlangit.id.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var mService: Messenger? = null
    private lateinit var mBoundServiceIntent: Intent
    private var mServiceBound = false

    private val mServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            mServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            mServiceBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBoundServiceIntent = Intent(this, MediaService::class.java)
        mBoundServiceIntent.action = MediaService.ACTION_CREATE

        startService(mBoundServiceIntent)
        bindService(mBoundServiceIntent,mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    fun onPlay(view: View) {
        if(!mServiceBound) return
        try {
            mService?.send(Message.obtain(null, MediaService.PLAY, 0, 0))
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    fun onStop(view: View) {
        if(!mServiceBound) return
        try {
            mService?.send(Message.obtain(null, MediaService.STOP, 0, 0))
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBoundServiceIntent.action = MediaService.ACTION_DESTROY

        startService(mBoundServiceIntent)
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
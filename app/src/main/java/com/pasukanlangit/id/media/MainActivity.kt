package com.pasukanlangit.id.media

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var mMediaPlayer: MediaPlayer? = null
    private var isReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
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
        }

        mMediaPlayer?.setOnErrorListener { _, _, _ -> false }

    }

    fun onPlay(view: View) {
        if(!isReady){
            mMediaPlayer?.prepareAsync()
        }else{
            if(mMediaPlayer?.isPlaying == true){
                mMediaPlayer?.pause()
            }else{
                mMediaPlayer?.start()
            }
        }
    }
    fun onStop(view: View) {
        if(mMediaPlayer?.isPlaying == true || isReady){
            mMediaPlayer?.stop()
            isReady = false
        }
    }
}
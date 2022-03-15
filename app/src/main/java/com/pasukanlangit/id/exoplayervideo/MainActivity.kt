package com.pasukanlangit.id.exoplayervideo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.pasukanlangit.id.exoplayervideo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE){
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val player = ExoPlayer.Builder(this).build()
        viewBinding.videoView.player = player

        val mediaItem = MediaItem.fromUri(URL_VIDEO_DICODING)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    companion object {
        const val URL_VIDEO_DICODING = "https://github.com/dicodingacademy/assets/releases/download/release-video/VideoDicoding.mp4"
    }
}
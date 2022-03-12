package com.pasukanlangit.id.media

import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var sp: SoundPool
    private var soundId: Int = 0
    private var spLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sp = SoundPool.Builder()
            .setMaxStreams(10)
            .build()

        sp.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if(status == 0){
                spLoaded = true
            }else{
                Toast.makeText(this@MainActivity, "Gagal load", Toast.LENGTH_SHORT).show()
            }
        }

        soundId = sp.load(this, R.raw.clinking_glasses, 1)
    }

    fun onPlay(view: View) {
        if(spLoaded){
            sp.play(soundId, 1f,1f,0,0,1f)
        }
    }
}
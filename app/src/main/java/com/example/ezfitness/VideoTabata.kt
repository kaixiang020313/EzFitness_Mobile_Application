package com.example.ezfitness

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class VideoTabata : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_tabata)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        val youTubePlayerView: YouTubePlayerView = findViewById(R.id.tabataView)
        lifecycle.addObserver(youTubePlayerView)

        val addVideoTabata: FloatingActionButton = findViewById(R.id.addVideoT)
        addVideoTabata.setOnClickListener {
            val intent = Intent(this, UploadVideoTabata::class.java)
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}

package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class VideoPushUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_push_up)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val pushupPlayer: YouTubePlayerView = findViewById(R.id.pushupView)
        lifecycle.addObserver(pushupPlayer)

        val addVideoPushup: FloatingActionButton = findViewById(R.id.addVideoP)
        addVideoPushup.setOnClickListener {
            val intent = Intent(this, UploadVideoPushup::class.java)
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
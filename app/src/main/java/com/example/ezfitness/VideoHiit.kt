package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VideoHiit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_hiit)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val addVideoHiit: FloatingActionButton = findViewById(R.id.addVideoH)
        addVideoHiit.setOnClickListener {
            val intent = Intent(this, UploadVideoHiit::class.java)
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
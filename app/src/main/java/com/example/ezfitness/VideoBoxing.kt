package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VideoBoxing : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_boxing)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val addVideoBoxing: FloatingActionButton = findViewById(R.id.addVideoB)
        addVideoBoxing.setOnClickListener {
            val intent = Intent(this, UploadVideoBoxing::class.java)
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
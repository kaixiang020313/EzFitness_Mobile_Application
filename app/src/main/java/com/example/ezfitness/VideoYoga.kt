package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VideoYoga : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_yoga)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        val addVideoYoga: FloatingActionButton = findViewById(R.id.addVideoY)
        addVideoYoga.setOnClickListener {
            val intent = Intent(this, UploadVideoYoga::class.java)
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VideoPilates : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_pilates)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val addVideoPilates: FloatingActionButton = findViewById(R.id.addVideoPi)
        addVideoPilates.setOnClickListener {
            val intent = Intent(this, UploadVideoPilates::class.java)
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VideoLeg : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_leg)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val addVideoLeg: FloatingActionButton = findViewById(R.id.addVideoL)
        addVideoLeg.setOnClickListener {
            val intent = Intent(this, UploadVideoLeg::class.java)
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
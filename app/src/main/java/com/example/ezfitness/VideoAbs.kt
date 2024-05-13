package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class VideoAbs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_abs)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        val addVideoAbs: FloatingActionButton = findViewById(R.id.addVideoA)
        addVideoAbs.setOnClickListener {
            val intent = Intent(this, UploadVideoAbs::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
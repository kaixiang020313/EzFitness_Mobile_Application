package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView

class Pilates : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilates)

        val imagePilates = findViewById<ImageView>(R.id.iPilates)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        // 为按钮添加点击事件
        imagePilates.setOnClickListener {
            // 创建意图，跳转到 VideoTabata 活动
            val intent = Intent(this, VideoPilates::class.java)
            // 启动 VideoTabata 活动
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
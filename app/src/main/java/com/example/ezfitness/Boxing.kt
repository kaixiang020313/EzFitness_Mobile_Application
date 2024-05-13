package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView

class Boxing : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boxing)

        val imageBoxing = findViewById<ImageView>(R.id.iBoxing)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // 为按钮添加点击事件
        imageBoxing.setOnClickListener {
            // 创建意图，跳转到 VideoTabata 活动
            val intent = Intent(this, VideoBoxing::class.java)
            // 启动 VideoTabata 活动
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
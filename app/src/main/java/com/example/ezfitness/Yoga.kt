package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView

class Yoga : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yoga)

        val imageYoga = findViewById<ImageView>(R.id.iyoga)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // 为按钮添加点击事件
        imageYoga.setOnClickListener {
            // 创建意图，跳转到 VideoTabata 活动
            val intent = Intent(this, VideoYoga::class.java)
            // 启动 VideoTabata 活动
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            // 返回前一页
            finish()
        }
    }
}
package com.example.ezfitness

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class WorkUpPreview : AppCompatActivity() {
    // 声明数据库引用
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_up_preview)

        // 获取 Firebase 实时数据库的引用
        databaseRef = FirebaseDatabase.getInstance("https://ezfitness-78842-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        // 获取按钮
        val tabataButton: Button = findViewById(R.id.tabataButton)
        val btnBack: ImageButton = findViewById(R.id.btnBack)

        // 为按钮添加点击事件
        tabataButton.setOnClickListener {
            // 创建意图，跳转到 VideoTabata 活动
            val intent = Intent(this, VideoTabata::class.java)
            // 启动 VideoTabata 活动
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }
        // 获取并显示从 Firebase 实时数据库读取的数据
        readDataFromDatabase()
    }

    private fun readDataFromDatabase() {
        // 获取 "p1" 节点的引用
        val p1Ref = databaseRef.child("workout").child("p1")

        // 添加数据变化的监听器
        p1Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 读取 "p1" 节点下的 textCal1 和 textMin1 字段的值
                val textCal1Value = snapshot.child("textCal1").value.toString()
                val textMin1Value = snapshot.child("textMin1").value.toString()

                // 更新 TextView 显示的值
                val textCal1: TextView = findViewById(R.id.textCal1)
                val textMin1: TextView = findViewById(R.id.textMin1)
                textCal1.text = textCal1Value
                textMin1.text = textMin1Value
            }

            override fun onCancelled(error: DatabaseError) {
                // 读取数据失败
                // 可以添加一些日志记录或者错误处理逻辑
            }
        })
    }
}

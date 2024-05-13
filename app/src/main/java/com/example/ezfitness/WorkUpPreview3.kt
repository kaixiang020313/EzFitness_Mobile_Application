package com.example.ezfitness

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WorkUpPreview3 : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_up_preview3)

        databaseRef = FirebaseDatabase.getInstance("https://ezfitness-78842-default-rtdb.asia-southeast1.firebasedatabase.app/").reference


        val legButton: Button = findViewById(R.id.legButton)
        val btnBack: ImageButton = findViewById(R.id.btnBack)

        // 为按钮添加点击事件
        legButton.setOnClickListener {
            // 创建意图，跳转到 VideoTabata 活动
            val intent = Intent(this, VideoLeg::class.java)
            // 启动 VideoTabata 活动
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            onBackPressed()
        }
        readDataFromDatabase()
    }

    private fun readDataFromDatabase() {
        val p3Ref = databaseRef.child("workout").child("p3")


        p3Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val textCal3Value = snapshot.child("textCal3").value.toString()
                val textMin3Value = snapshot.child("textMin3").value.toString()


                val textCal3: TextView = findViewById(R.id.textCal3)
                val textMin3: TextView = findViewById(R.id.textMin3)
                textCal3.text = textCal3Value
                textMin3.text = textMin3Value
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

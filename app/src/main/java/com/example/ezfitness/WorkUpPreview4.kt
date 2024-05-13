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

class WorkUpPreview4 : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_up_preview4)

        databaseRef = FirebaseDatabase.getInstance("https://ezfitness-78842-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val pushupButton: Button = findViewById(R.id.pushupButton)
        val btnBack: ImageButton = findViewById(R.id.btnBack)


        // 为按钮添加点击事件
        pushupButton.setOnClickListener {
            // 创建意图，跳转到 VideoTabata 活动
            val intent = Intent(this, VideoPushUp::class.java)
            // 启动 VideoTabata 活动
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            onBackPressed()
        }
        readDataFromDatabase()
    }


    private fun readDataFromDatabase() {
        val p4Ref = databaseRef.child("workout").child("p4")


        p4Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val textCal4Value = snapshot.child("textCal4").value.toString()
                val textMin4Value = snapshot.child("textMin4").value.toString()


                val textCal4: TextView = findViewById(R.id.textCal4)
                val textMin4: TextView = findViewById(R.id.textMin4)
                textCal4.text = textCal4Value
                textMin4.text = textMin4Value
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}
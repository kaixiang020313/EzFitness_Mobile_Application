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

class WorkUpPreview2 : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_up_preview2)

        databaseRef = FirebaseDatabase.getInstance("https://ezfitness-78842-default-rtdb.asia-southeast1.firebasedatabase.app/").reference


        val absButton: Button = findViewById(R.id.absButton)
        val btnBack: ImageButton = findViewById(R.id.btnBack)

        // 为按钮添加点击事件
        absButton.setOnClickListener {
            // 创建意图，跳转到 VideoTabata 活动
            val intent = Intent(this, VideoAbs::class.java)
            // 启动 VideoTabata 活动
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            onBackPressed()
        }
        readDataFromDatabase()
    }

    private fun readDataFromDatabase() {
        val p2Ref = databaseRef.child("workout").child("p2")


        p2Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val textCal1Value = snapshot.child("textCal2").value.toString()
                val textMin1Value = snapshot.child("textMin2").value.toString()


                val textCal2: TextView = findViewById(R.id.textCal2)
                val textMin2: TextView = findViewById(R.id.textMin2)
                textCal2.text = textCal1Value
                textMin2.text = textMin1Value
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}
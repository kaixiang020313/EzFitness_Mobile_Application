package com.example.ezfitness

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.app.DatePickerDialog
import android.widget.Button
import android.widget.EditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Planner : AppCompatActivity() {

    private lateinit var textDate: TextView
    private lateinit var viewCalendar: ImageButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var editTextList: List<EditText>
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var selectedDate: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planner)

        // 初始化 Firebase 实例并启用持久性存储
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        databaseReference = FirebaseDatabase.getInstance("https://ezfitness-78842-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("plan")

        val btnBack: ImageButton = findViewById(R.id.btnBack)

        // 初始化视图
        textDate = findViewById(R.id.textDate)
        viewCalendar = findViewById(R.id.viewCalendar)

        // 初始化Firebase Auth和Firestore实例
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 初始化选择的日期
        selectedDate = Calendar.getInstance()

        // 更新日期范围
        updateDateRange()

        // 设置日历图标的点击监听器
        viewCalendar.setOnClickListener {
            showDatePickerDialog()
        }

        // 设置“确定”按钮的点击监听器
        val buttonOk = findViewById<Button>(R.id.buttonOk)
        buttonOk.setOnClickListener {
            saveDataToFirebase()
            sendEmail()
        }

        // 设置“取消”按钮的点击监听器
        val buttonCannot = findViewById<Button>(R.id.buttonCannot)
        buttonCannot.setOnClickListener {
            // 清除所有 EditText 中的文本内容
            editTextList.forEach { it.text.clear() }
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }

        // 初始化 EditText 列表
        editTextList = listOf<EditText>(
            findViewById(R.id.editSun),
            findViewById(R.id.editMon),
            findViewById(R.id.editTus),
            findViewById(R.id.editWed),
            findViewById(R.id.editThu),
            findViewById(R.id.editFri),
            findViewById(R.id.editSat)
        )
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                selectedDate.set(year, monthOfYear, dayOfMonth)
                updateDateRange()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateDateRange() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = sdf.format(selectedDate.time)
        val endOfWeek = selectedDate.clone() as Calendar
        endOfWeek.add(Calendar.DATE, 6)
        val endDate = sdf.format(endOfWeek.time)
        textDate.text = "${startDate}_to_${endDate}"
    }

    private fun saveDataToFirebase() {
        val sdfFirebase = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateFirebase = sdfFirebase.format(selectedDate.time)
        for ((index, editText) in editTextList.withIndex()) {
            val dayOfWeek = getDayOfWeek(index)
            val value = editText.text.toString()
            databaseReference.child("plan").child(dateFirebase).child(dayOfWeek).setValue(value)
        }
        showToast("Upload Successful!")
    }

    // 获取当前用户的电子邮件地址
    private fun getCurrentUserEmail(callback: (String?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val email = currentUser.email
            callback(email)
        } else {
            callback(null)
        }
    }

    private fun sendEmail() {
        // 获取当前用户的电子邮件地址
        getCurrentUserEmail { recipientEmail ->
            if (!recipientEmail.isNullOrEmpty()) {
                // 获取最新上传的日期和数据
                getLatestUploadData { latestDate, latestData ->
                    // 发送包含最新上传信息的邮件
                    sendLatestUploadEmailWithDetails(recipientEmail, latestDate, latestData)
                }
            } else {
                showToast("Failed to get user email.")
            }
        }
    }

    private fun getLatestUploadData(callback: (String?, Map<String, Any>?) -> Unit) {
        // 获取 "plan" 路径下的所有数据
        databaseReference.child("plan").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var latestDate: String? = null
                var latestData: Map<String, Any>? = null

                // 遍历每个日期节点，找到最新的日期和数据
                for (dateSnapshot in snapshot.children) {
                    val date = dateSnapshot.key
                    if (date != null && (latestDate == null || date > latestDate)) {
                        latestDate = date
                        latestData = dateSnapshot.value as Map<String, Any>?
                    }
                }

                // 调用回调函数，传递最新的日期和数据
                callback(latestDate, latestData)
            }

            override fun onCancelled(error: DatabaseError) {
                // 处理错误
                Log.e(TAG, "Error fetching latest upload data: ${error.message}")
                callback(null, null)
            }
        })
    }

    private fun sendLatestUploadEmailWithDetails(recipientEmail: String, latestDate: String?, latestData: Map<String, Any>?) {
        // 如果最新日期和数据不为空，则构建邮件内容并发送邮件
        if (latestDate != null && latestData != null) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "message/rfc822" // 设置邮件格式
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail)) // 设置收件人邮箱
            intent.putExtra(Intent.EXTRA_SUBJECT, "Latest Data Uploaded to Planner") // 设置邮件主题

            // 构建邮件内容
            var emailContent = "Latest data uploaded to planner:\n\n" +
                    "Date: $latestDate\n"

            // 添加最新日期下的键值对信息
            for ((key, value) in latestData) {
                emailContent += "$key: $value\n"
            }

            intent.putExtra(Intent.EXTRA_TEXT, emailContent) // 设置邮件内容

            // 启动邮件应用
            startActivity(Intent.createChooser(intent, "Send Email"))
        } else {
            showToast("Failed to get latest upload data.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getDayOfWeek(index: Int): String {
        return when (index) {
            0 -> "Sun"
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            else -> throw IllegalArgumentException("Invalid index")
        }
    }

    companion object {
        private const val TAG = "PlannerActivity"
    }
}

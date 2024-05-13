package com.example.ezfitness

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

class InbodyReport : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PICK_PHOTO = 1001
    }

    private lateinit var databaseReference: DatabaseReference

    // 声明 EditText 字段
    private lateinit var editIW: EditText
    private lateinit var editEW: EditText
    private lateinit var editTBW: EditText
    private lateinit var editDLM: EditText
    private lateinit var editLBM: EditText
    private lateinit var editBFM: EditText
    private lateinit var editWeight: EditText
    private lateinit var editSMM: EditText
    private lateinit var editBMI: EditText
    private lateinit var editPBF: EditText
    private lateinit var editRA: EditText
    private lateinit var editLA: EditText
    private lateinit var editT: EditText
    private lateinit var editRL: EditText
    private lateinit var editLL: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbody_report)

        // 初始化数据库引用，注意更改数据库 URL
        databaseReference =
            FirebaseDatabase.getInstance("https://ezfitness-78842-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("inbody_report")

        // 找到按钮并添加点击事件监听器
        val selectPhotoButton: FloatingActionButton = findViewById(R.id.select_photo)
        selectPhotoButton.setOnClickListener {
            dispatchPickPhotoIntent()
        }

        // 初始化 EditText 字段
        editIW = findViewById(R.id.editIW)
        editEW = findViewById(R.id.editEW)
        editTBW = findViewById(R.id.editTBW)
        editDLM = findViewById(R.id.editDLM)
        editLBM = findViewById(R.id.editLBM)
        editBFM = findViewById(R.id.editBFM)
        editWeight = findViewById(R.id.editWeight)
        editSMM = findViewById(R.id.editSMM)
        editBMI = findViewById(R.id.editBMI)
        editPBF = findViewById(R.id.editPBF)
        editRA = findViewById(R.id.editRA)
        editLA = findViewById(R.id.editLA)
        editT = findViewById(R.id.editT)
        editRL = findViewById(R.id.editRL)
        editLL = findViewById(R.id.editLL)

        // 找到 "Done" 按钮并添加点击事件监听器
        val doneButton: Button = findViewById(R.id.done)
        doneButton.setOnClickListener {
            // 保存测量数据到 Firebase Realtime Database
            saveDataToDatabase()
        }

        // 找到 "Back" 按钮并添加点击事件监听器
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            // 返回前一页
            onBackPressed()
        }
    }

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val photoPath = getAbsolutePathFromUri(uri)
                    photoPath?.let { path ->
                        uploadImage(File(path))
                    }
                }
            }
        }

    private fun dispatchPickPhotoIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        pickPhotoLauncher.launch(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_PHOTO && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val photoPath = getAbsolutePathFromUri(uri)
                photoPath?.let { path ->
                    uploadImage(File(path))
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun getAbsolutePathFromUri(uri: Uri): String? {
        val cursor =
            contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA))
            }
        }
        return null
    }

    private fun uploadImage(imageFile: File) {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.png",
                RequestBody.create("image/png".toMediaTypeOrNull(), imageFile))
            .build()


        val request = Request.Builder()
            .url("http://192.168.1.82:5000/process_image")
            .post(requestBody)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UploadImage", "Failed to upload image: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val jsonData = responseBody.string()
                    // 在这里处理返回的 JSON 数据
                    Log.d("UploadImage", "Response JSON: $jsonData")
                    try {
                        val jsonObject = JSONObject(jsonData)
                        runOnUiThread {
                            fillDataToEditTextFields(jsonObject)
                        }
                    } catch (e: JSONException) {
                        Log.e("UploadImage", "Failed to parse JSON: ${e.message}")
                    }
                }
            }
        })
    }

    private fun fillDataToEditTextFields(dataSnapshot: JSONObject) {
        val editIW: EditText = findViewById(R.id.editIW)
        val editEW: EditText = findViewById(R.id.editEW)
        val editTBW: EditText = findViewById(R.id.editTBW)
        val editDLM: EditText = findViewById(R.id.editDLM)
        val editLBM: EditText = findViewById(R.id.editLBM)
        val editBFM: EditText = findViewById(R.id.editBFM)
        val editWeight: EditText = findViewById(R.id.editWeight)
        val editSMM: EditText = findViewById(R.id.editSMM)
        val editBMI: EditText = findViewById(R.id.editBMI)
        val editPBF: EditText = findViewById(R.id.editPBF)
        val editRA: EditText = findViewById(R.id.editRA)
        val editLA: EditText = findViewById(R.id.editLA)
        val editT: EditText = findViewById(R.id.editT)
        val editRL: EditText = findViewById(R.id.editRL)
        val editLL: EditText = findViewById(R.id.editLL)

        editIW.setText("${dataSnapshot.optString("Region 1")} kg")
        editEW.setText("${dataSnapshot.optString("Region 2")} kg")
        editTBW.setText("${dataSnapshot.optString("Region 3")} kg")
        editDLM.setText("${dataSnapshot.optString("Region 4")} kg")
        editLBM.setText("${dataSnapshot.optString("Region 5")} kg")
        editBFM.setText("${dataSnapshot.optString("Region 6")} kg")
        editWeight.setText("${dataSnapshot.optString("Region 7")} kg")
        editSMM.setText("${dataSnapshot.optString("Region 8")} %")
        editBMI.setText("${dataSnapshot.optString("Region 9")} kg")
        editPBF.setText("${dataSnapshot.optString("Region 10")} %")
        editRA.setText("${dataSnapshot.optString("Region 11")} ECW/TBW")
        editLA.setText("${dataSnapshot.optString("Region 12")} ECW/TBW")
        editT.setText("${dataSnapshot.optString("Region 13")} ECW/TBW")
        editRL.setText("${dataSnapshot.optString("Region 14")} ECW/TBW")
        editLL.setText("${dataSnapshot.optString("Region 15")} ECW/TBW")

        // 启用用户输入
        enableEditTextFields()
    }

    // 填充数据到 EditText 字段后，启用用户输入
    private fun enableEditTextFields() {
        editIW.isEnabled = true
        editEW.isEnabled = true
        editTBW.isEnabled = true
        editDLM.isEnabled = true
        editLBM.isEnabled = true
        editBFM.isEnabled = true
        editWeight.isEnabled = true
        editSMM.isEnabled = true
        editBMI.isEnabled = true
        editPBF.isEnabled = true
        editRA.isEnabled = true
        editLA.isEnabled = true
        editT.isEnabled = true
        editRL.isEnabled = true
        editLL.isEnabled = true
    }

    // 将测量数据保存到 Firebase Realtime Database
    private fun saveDataToDatabase() {
        // 获取当前时间戳作为照片名称
        val photoName = System.currentTimeMillis().toString()

        // 创建一个新的数据项，其中键为照片的名称
        val newData = mapOf(
            "Intracellular Water" to editIW.text.toString(),
            "Extracellular Water" to editEW.text.toString(),
            "Total Body Water" to editTBW.text.toString(),
            "Dry Lean Mass" to editDLM.text.toString(),
            "Lean Body Mass" to editLBM.text.toString(),
            "Body Fat Mass" to editBFM.text.toString(),
            "Weight" to editWeight.text.toString(),
            "SMM" to editSMM.text.toString(),
            "BMI" to editBMI.text.toString(),
            "PBF" to editPBF.text.toString(),
            "Right Arm" to editRA.text.toString(),
            "Left Arm" to editLA.text.toString(),
            "Trunk" to editT.text.toString(),
            "Right leg" to editRL.text.toString(),
            "Left Leg" to editLL.text.toString()
        )

        // 将新数据项存储到数据库中
        databaseReference.child(photoName).setValue(newData)
            .addOnSuccessListener {
                // 数据存储成功
                Log.d("InbodyReport", "Data saved successfully")
                // 显示 Toast 信息
                showToast("Upload successful")
            }
            .addOnFailureListener { exception ->
                // 处理数据存储失败的情况
                Log.e("InbodyReport", "Failed to save data: ${exception.message}")
            }
    }

    // 辅助函数：显示 Toast 信息
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

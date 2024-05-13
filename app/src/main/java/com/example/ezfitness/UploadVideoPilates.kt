package com.example.ezfitness

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class UploadVideoPilates : AppCompatActivity() {

    private var selectedVideoUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var processPilates: LinearProgressIndicator
    private var latestUploadedVideoRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_video_pilates)

        val buttonSelect: Button = findViewById(R.id.buttonSelectPi)
        val buttonUpload: Button = findViewById(R.id.buttonUploadPi)
        val buttonCancel: Button = findViewById(R.id.buttonCancelPi)
        val uploadPilates: VideoView = findViewById(R.id.uploadPilates)
        processPilates = findViewById(R.id.processPilates)

        storageReference = FirebaseStorage.getInstance().reference

        buttonSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "video/*"
            startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO)
        }

        buttonUpload.setOnClickListener {
            selectedVideoUri?.let { uri ->
                processPilates.visibility = View.VISIBLE
                uploadVideoToFirebase(uri)
            }
        }

        buttonCancel.setOnClickListener {
            uploadPilates.stopPlayback()
            uploadPilates.setVideoURI(null)
            selectedVideoUri = null
            processPilates.visibility = View.GONE
            buttonUpload.isEnabled = false
            buttonCancel.isEnabled = false
            // Show cancel success message
            // Example: Toast.makeText(this, "Upload canceled", Toast.LENGTH_SHORT).show()
        }
        // 返回按钮的点击事件监听器
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun uploadVideoToFirebase(videoUri: Uri) {
        val videoRef = storageReference.child("pilates/${UUID.randomUUID()}")
        latestUploadedVideoRef = videoRef // 更新最新上传的视频引用
        val uploadTask = videoRef.putFile(videoUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                callPython(downloadUrl)
            }.addOnFailureListener { exception ->
                Log.e("Download URL", "Failed to get download URL: ${exception.message}")
            }
        }.addOnFailureListener { exception ->
            Log.e("Upload Video", "Upload failed: ${exception.message}")
        }.addOnProgressListener { taskSnapshot ->
            // Update progress
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            processPilates.progress = progress
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedVideoUri = uri
                val uploadPilates: VideoView = findViewById(R.id.uploadPilates)
                uploadPilates.setVideoURI(uri)
                uploadPilates.setOnPreparedListener {
                    uploadPilates.start()
                }
                findViewById<Button>(R.id.buttonUploadPi).isEnabled = true
                findViewById<Button>(R.id.buttonCancelPi).isEnabled = true
            }
        }
    }

    private fun callPython(video_path: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(50, TimeUnit.SECONDS) // 设置连接超时时间为 30 秒
            .writeTimeout(50, TimeUnit.SECONDS) // 设置写入超时时间为 30 秒
            .readTimeout(50, TimeUnit.SECONDS) // 设置读取超时时间为 30 秒
            .build()

        val requestBody = FormBody.Builder()
            .add("video_url", video_path)
            .add("operation", "Yoga") // 设置 operation 参数为 "Yoga"
            .build()

        val request = Request.Builder()
            .url("http://192.168.1.82:5000/detect_movement")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UploadImage", "Failed to upload image: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val jsonData = responseBody.string()
                    Log.d("UploadImage", "Response JSON: $jsonData")
                    try {
                        val jsonObject = JSONObject(jsonData)
                        if (jsonObject.has("Yoga")) {
                            val isYoga = jsonObject.getBoolean("Yoga") // 获取 Yoga 动作检测结果

                            runOnUiThread {
                                // 根据检测结果显示 Toast 消息
                                if (isYoga) {
                                    showToast("Pilates action detected")
                                } else {
                                    showToast("The video does not contain Pilates action")
                                    // 如果未检测到 Yoga 动作，删除已上传的视频
                                    latestUploadedVideoRef?.let { deleteUploadedVideo(it) }
                                }
                            }
                        } else {
                            // 如果 JSON 数据中没有指示检测到人体动作的信息，则显示相应的 Toast 消息
                            runOnUiThread {
                                showToast("No person detected in the video")
                            }
                        }
                    } catch (e: JSONException) {
                        Log.e("UploadImage", "Failed to parse JSON: ${e.message}")
                    }
                }
            }
        })
    }

    companion object {
        private const val REQUEST_CODE_SELECT_VIDEO = 100
    }
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@UploadVideoPilates, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 辅助函数：删除已上传的视频
    private fun deleteUploadedVideo(videoRef: StorageReference) {
        videoRef.delete().addOnSuccessListener {
            Log.d("Delete Video", "Video deleted successfully")
        }.addOnFailureListener { exception ->
            Log.e("Delete Video", "Failed to delete video: ${exception.message}")
        }
    }
}
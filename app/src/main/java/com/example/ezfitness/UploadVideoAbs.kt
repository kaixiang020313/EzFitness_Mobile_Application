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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class UploadVideoAbs : AppCompatActivity() {

    private var selectedVideoUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var processAbs: LinearProgressIndicator
    private var latestUploadedVideoRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_video_abs)

        val buttonSelect: Button = findViewById(R.id.buttonSelectA)
        val buttonUpload: Button = findViewById(R.id.buttonUploadA)
        val buttonCancel: Button = findViewById(R.id.buttonCancelA)
        val uploadLeg: VideoView = findViewById(R.id.uploadAbs)

        processAbs = findViewById(R.id.processAbs)

        storageReference = FirebaseStorage.getInstance().reference

        buttonSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "video/*"
            startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO)
        }

        buttonUpload.setOnClickListener {
            selectedVideoUri?.let { uri ->
                processAbs.visibility = View.VISIBLE
                uploadVideoToFirebase(uri)
            }
        }

        buttonCancel.setOnClickListener {
            uploadLeg.stopPlayback()
            uploadLeg.setVideoURI(null)
            selectedVideoUri = null
            processAbs.visibility = View.GONE
            buttonUpload.isEnabled = false
            buttonCancel.isEnabled = false
        }

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun uploadVideoToFirebase(videoUri: Uri) {
        val videoRef = storageReference.child("abs/${UUID.randomUUID()}")
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
            // 如果上传失败，删除已上传的视频
            deleteUploadedVideo(videoRef)
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            processAbs.progress = progress
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedVideoUri = uri
                val uploadAbs: VideoView = findViewById(R.id.uploadAbs)
                uploadAbs.setVideoURI(uri)
                uploadAbs.setOnPreparedListener {
                    uploadAbs.start()
                }
                findViewById<Button>(R.id.buttonUploadA).isEnabled = true
                findViewById<Button>(R.id.buttonCancelA).isEnabled = true
            }
        }
    }

    private fun callPython(video_path: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 设置连接超时时间为 30 秒
            .writeTimeout(30, TimeUnit.SECONDS) // 设置写入超时时间为 30 秒
            .readTimeout(30, TimeUnit.SECONDS) // 设置读取超时时间为 30 秒
            .build()

        val requestBody = FormBody.Builder()
            .add("video_url", video_path)
            .add("operation", "Abs")
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
                        if (jsonObject.has("Abs")) {
                            val isAbs = jsonObject.getBoolean("Abs")
                            runOnUiThread {
                                if (isAbs) {
                                    showToast("Abs action detected")
                                } else {
                                    showToast("This video does not contain Abs action")
                                    // 如果未检测到 Abs 动作，删除已上传的视频
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
            Toast.makeText(this@UploadVideoAbs, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteUploadedVideo(videoRef: StorageReference) {
        videoRef.delete().addOnSuccessListener {
            Log.d("Delete Video", "Video deleted successfully")
        }.addOnFailureListener { exception ->
            Log.e("Delete Video", "Failed to delete video: ${exception.message}")
        }
    }
}

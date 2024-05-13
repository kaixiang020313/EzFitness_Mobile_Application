package com.example.ezfitness

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.regex.Pattern
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var editTextUsername: EditText
    private lateinit var editTextDateOfBirth: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var editTextHeight: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonResetPassword: Button
    private lateinit var buttonSave: Button
    private lateinit var buttonEditProfilePicture: Button
    private lateinit var imageViewProfile: ImageView

    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth)
        spinnerGender = findViewById(R.id.spinnerGender)
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextWeight = findViewById(R.id.editTextWeight)
        buttonBack = findViewById(R.id.buttonBack)
        buttonResetPassword = findViewById(R.id.buttonResetPassword)
        buttonSave = findViewById(R.id.buttonSave)
        buttonEditProfilePicture = findViewById(R.id.buttonEditProfilePicture)
        imageViewProfile = findViewById(R.id.imageViewProfile)

        editTextUsername.addValidation("Username", "^[a-zA-Z0-9_]*\$")
        editTextHeight.addValidation("Height", "^[0-9]+\$")
        editTextWeight.addValidation("Weight", "^[0-9]+\$")

        val adapterGender = ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        )
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapterGender

        val adapterActivityLevel = ArrayAdapter.createFromResource(
            this,
            R.array.activity_level_options,
            android.R.layout.simple_spinner_item
        )
        adapterActivityLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerActivityLevel.adapter = adapterActivityLevel

        buttonBack.setOnClickListener {
            finish()
        }

        buttonResetPassword.setOnClickListener {
            resetPassword()
        }

        buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveChanges()
            }
        }

        buttonEditProfilePicture.setOnClickListener {
            openGallery()
        }

        val imageButtonCalendar: ImageButton = findViewById(R.id.imageButtonCalendar)
        imageButtonCalendar.setOnClickListener {
            showDatePicker()
        }

        val userId = auth.currentUser?.uid
        userId?.let { fetchUserData(it) }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null && data.data != null) {
                    val selectedImageUri = data.data
                    selectedImageUri?.let {
                        uploadProfilePicture(it)
                    }
                } else {
                    showToast("Failed to retrieve image. Please try again.")
                }
            } else {
                showToast("Image selection canceled.")
            }
        }


    }

    private fun fetchUserData(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: ""
                    val dateOfBirth = document.getString("dateOfBirth") ?: ""
                    val gender = document.getString("gender") ?: ""
                    val activityLevel = document.getString("activityLevel") ?: ""
                    val height = document.getString("height") ?: ""
                    val weight = document.getString("weight") ?: ""
                    val profilePictureUrl = document.getString("profilePictureUrl")

                    editTextUsername.setText(username)
                    editTextDateOfBirth.setText(dateOfBirth)
                    val genderPosition = (spinnerGender.adapter as ArrayAdapter<String>).getPosition(gender)
                    spinnerGender.setSelection(genderPosition)
                    val activityLevelPosition = (spinnerActivityLevel.adapter as ArrayAdapter<String>).getPosition(activityLevel)
                    spinnerActivityLevel.setSelection(activityLevelPosition)
                    editTextHeight.setText(height)
                    editTextWeight.setText(weight)

                    profilePictureUrl?.let {
                        loadProfilePicture(it)
                    }
                }
            }
            .addOnFailureListener { exception ->
                showToast("Failed to fetch user data. Please try again later.")
            }
    }

    private fun loadProfilePicture(profilePictureUrl: String) {
        Glide.with(this@EditProfileActivity)
            .load(profilePictureUrl)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(imageViewProfile)
    }

    private fun saveChanges() {
        val userId = auth.currentUser?.uid
        userId?.let {
            val username = editTextUsername.text.toString()
            val dateOfBirth = editTextDateOfBirth.text.toString()
            val gender = spinnerGender.selectedItem.toString()
            val activityLevel = spinnerActivityLevel.selectedItem.toString()
            val height = editTextHeight.text.toString()
            val weight = editTextWeight.text.toString()

            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.update(
                mapOf(
                    "username" to username,
                    "dateOfBirth" to dateOfBirth,
                    "gender" to gender,
                    "activityLevel" to activityLevel,
                    "height" to height,
                    "weight" to weight
                )
            ).addOnSuccessListener {
                showToast("Changes saved successfully.")
                finish()
            }.addOnFailureListener { e ->
                showToast("Failed to save changes. Please try again later.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun resetPassword() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dateOfBirthText = editTextDateOfBirth.text.toString()
        if (dateOfBirthText.isNotEmpty()) {
            val dateOfBirthParts = dateOfBirthText.split("/")
            val day = dateOfBirthParts[0].toInt()
            val month = dateOfBirthParts[1].toInt() - 1
            val year = dateOfBirthParts[2].toInt()
            calendar.set(year, month, day)
        }

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                editTextDateOfBirth.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        resultLauncher?.launch(intent)
    }

    private fun uploadProfilePicture(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        userId?.let {
            val storageRef = storage.reference
            val imagesRef = storageRef.child("profile_pictures/${userId}/profile.jpg")

            imagesRef.putFile(imageUri)
                .addOnSuccessListener { _ ->
                    imagesRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val profilePictureUrl = downloadUri.toString()
                        updateUserProfilePicture(userId, profilePictureUrl)
                    }.addOnFailureListener {
                        showToast("Failed to get download URL. Please try again later.")
                    }
                }
                .addOnFailureListener {
                    showToast("Failed to upload image. Please try again later.")
                }
        }
    }

    private fun updateUserProfilePicture(userId: String, profilePictureUrl: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.update("profilePictureUrl", profilePictureUrl)
            .addOnSuccessListener {
                showToast("Profile picture updated successfully.")
                Glide.with(this@EditProfileActivity)
                    .load(profilePictureUrl)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(imageViewProfile)
            }.addOnFailureListener { e ->
                showToast("Failed to update profile picture URL. Please try again later.")
            }
    }

    private fun EditText.addValidation(fieldName: String, regexPattern: String) {
        this.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val input = this.text.toString().trim()
                if (!Pattern.matches(regexPattern, input)) {
                    this.error = "$fieldName is invalid"
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val username = editTextUsername.text.toString().trim()
        val dateOfBirth = editTextDateOfBirth.text.toString().trim()
        val height = editTextHeight.text.toString().trim()
        val weight = editTextWeight.text.toString().trim()

        if (username.isEmpty() || dateOfBirth.isEmpty() || height.isEmpty() || weight.isEmpty()) {
            showToast("Please fill in all fields.")
            return false
        }
        return true
    }
}

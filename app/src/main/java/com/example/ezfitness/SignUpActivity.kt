package com.example.ezfitness

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var buttonBack: ImageButton
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        firebaseFirestore = FirebaseFirestore.getInstance()

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        buttonBack = findViewById(R.id.buttonBack)

        buttonSignUp.setOnClickListener {
            signUp()
        }

        buttonBack.setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.imagePasswordToggle).setOnClickListener {
            togglePasswordVisibility()
        }

        findViewById<ImageView>(R.id.imageConfirmPasswordToggle).setOnClickListener {
            toggleConfirmPasswordVisibility()
        }
    }

    private fun signUp() {
        val username = editTextUsername.text.toString()
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()
        val confirmPassword = editTextConfirmPassword.text.toString()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUsernameToFirestore(username)
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "The email address is already in use", Toast.LENGTH_SHORT).show()
                    } else {
                        // Handle other authentication failures
                        Toast.makeText(this, "Sign up failed. Please try again later", Toast.LENGTH_SHORT).show()
                        Log.e("SignUp", "Failed to sign up", exception)
                    }
                }
            }
    }

    private fun saveUsernameToFirestore(username: String) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            Log.e("SaveUsername", "User UID is null")
            return
        }

        val user = hashMapOf(
            "username" to username
        )

        firebaseFirestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Account signed up successfully", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Log.e("SaveUsername", "Failed to save username: $e")
                Toast.makeText(this, "Failed to save username: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val visibility = if (isPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editTextPassword.inputType = visibility
        editTextPassword.setSelection(editTextPassword.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        val visibility = if (isConfirmPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editTextConfirmPassword.inputType = visibility
        editTextConfirmPassword.setSelection(editTextConfirmPassword.text.length)
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

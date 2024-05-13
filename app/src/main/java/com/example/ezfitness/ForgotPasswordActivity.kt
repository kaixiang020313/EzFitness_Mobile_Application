package com.example.ezfitness

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    // Define UI components
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSend: Button
    private lateinit var buttonBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize UI components
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSend = findViewById(R.id.buttonSend)
        buttonBack = findViewById(R.id.buttonBack)

        // Set on-click listener for the Send button
        buttonSend.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                showToast("Please enter your email address.")
            }
        }

        // Set on-click listener for the Back button
        buttonBack.setOnClickListener { finish() }
    }

    private fun sendPasswordResetEmail(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                // Notify the user to check their email
                showToast("If your email is registered, you'll receive instructions to reset your password.")
            }
            .addOnFailureListener { exception ->
                // Log the error for debugging purposes
                Log.e("ForgotPassword", "Failed to send password reset email", exception)
                // Notify the user that an error occurred
                showToast("An error occurred. Please try again later.")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

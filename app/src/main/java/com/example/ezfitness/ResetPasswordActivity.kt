package com.example.ezfitness

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var editTextOldPassword: EditText
    private lateinit var editTextNewPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonResetPassword: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()

        editTextOldPassword = findViewById(R.id.editTextOldPassword)
        editTextNewPassword = findViewById(R.id.editTextNewPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmNewPassword)
        buttonResetPassword = findViewById(R.id.buttonResetPassword)

        buttonResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val oldPassword = editTextOldPassword.text.toString()
        val newPassword = editTextNewPassword.text.toString()
        val confirmPassword = editTextConfirmPassword.text.toString()

        if (newPassword.isEmpty()) {
            showToast("Please enter a new password")
            return
        }

        if (newPassword.length < 6) {
            showToast("Password must be at least 6 characters long")
            return
        }

        if (newPassword != confirmPassword) {
            showToast("Passwords do not match")
            return
        }

        val user = auth.currentUser
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    showToast("Password updated successfully")
                                    finish()
                                } else {
                                    showToast("Failed to update password. Please try again.")
                                }
                            }
                    } else {
                        showToast("Authentication failed. Please check your old password.")
                    }
                }
        } else {
            showToast("User not found. Please log in again.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


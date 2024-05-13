package com.example.ezfitness

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.text.SpannableString
import android.content.Intent
import android.text.InputType
import android.text.Spannable
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import android.widget.Toast
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsernameEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSignIn: Button
    private lateinit var textViewSignUp: TextView
    private var isPasswordVisible = false
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsernameEmail = findViewById(R.id.editTextUsernameEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonSignIn = findViewById(R.id.buttonSignIn)
        textViewSignUp = findViewById(R.id.textViewSignUp)

        buttonSignIn.setOnClickListener {
            signIn()
        }

        val textViewForgotPassword = findViewById<TextView>(R.id.textViewForgotPassword)
        textViewForgotPassword.setOnClickListener {
            onForgotPasswordClick(it)
        }

        val textViewSignUp = findViewById<TextView>(R.id.textViewSignUp)
        val text = textViewSignUp.text.toString()
        val index = text.indexOf("Sign Up")

        val spannableString = SpannableString(text)
              val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        navigateToSignUp()
            }
        }
        spannableString.setSpan(clickableSpan, index, index + "Sign Up".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textViewSignUp.text = spannableString
        textViewSignUp.movementMethod = LinkMovementMethod.getInstance()

        val imagePasswordToggle = findViewById<ImageView>(R.id.imagePasswordToggle)
        imagePasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility()
        }

    }

    private fun signIn() {
        val email = editTextUsernameEmail.text.toString()
        val password = editTextPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMainActivity(R.id.menu_home)
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Authentication failed. Please try again later", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun onForgotPasswordClick(view: View) {
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }

    private fun navigateToMainActivity(destinationFragment: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("destinationFragment", destinationFragment)
        startActivity(intent)
        finish()
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        // Move cursor to the end of the text
        editTextPassword.setSelection(editTextPassword.text.length)
    }


}

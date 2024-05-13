package com.example.ezfitness

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class NewMealActivity : AppCompatActivity() {

    private lateinit var editTextMealName: EditText
    private lateinit var editTextCalories: EditText
    private lateinit var editTextCarbs: EditText
    private lateinit var editTextProtein: EditText
    private lateinit var editTextFats: EditText
    private lateinit var btnSave: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_meal)

        // Initialize views
        editTextMealName = findViewById(R.id.editTextMealName)
        editTextCalories = findViewById(R.id.editTextCalories)
        editTextCarbs = findViewById(R.id.editTextCarbs)
        editTextProtein = findViewById(R.id.editTextProtein)
        editTextFats = findViewById(R.id.editTextFats)
        btnSave = findViewById(R.id.btnSave)

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set onClickListener for Save button
        btnSave.setOnClickListener { saveMealData() }
        setupBackButton()
    }

    private fun saveMealData() {
        // Get the input values
        val mealName = editTextMealName.text.toString().trim()
        val caloriesText = editTextCalories.text.toString().trim()
        val carbsText = editTextCarbs.text.toString().trim()
        val proteinText = editTextProtein.text.toString().trim()
        val fatsText = editTextFats.text.toString().trim()

        // Validate input fields
        if (mealName.isEmpty() || caloriesText.isEmpty() || carbsText.isEmpty() ||
            proteinText.isEmpty() || fatsText.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val calories = caloriesText.toDoubleOrNull()
        val carbs = carbsText.toDoubleOrNull()
        val protein = proteinText.toDoubleOrNull()
        val fat = fatsText.toDoubleOrNull()

        if (calories == null || carbs == null || protein == null || fat == null) {
            Toast.makeText(this, "Invalid input for numerical fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (calories <= 0 || carbs <= 0 || protein <= 0 || fat <= 0) {
            Toast.makeText(
                this,
                "Values for numerical fields must be greater than zero",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val currentDate = Date()

        // Create a map to store meal data with date
        val mealData = hashMapOf(
            "mealName" to mealName,
            "calories" to calories,
            "carbs" to carbs,
            "protein" to protein,
            "fat" to fat,
            "dateAdded" to currentDate
        )

        // Save data to Firestore
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId)
                .collection("dietaryInformation").add(mealData)
                .addOnSuccessListener { documentReference ->
                    // Data saved successfully
                    // You can add further handling here if needed
                    Toast.makeText(this, "Meal data saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    // Error occurred while saving data
                    // You can add error handling here if needed
                    Log.e("NewMealActivity", "Error saving meal data", e)
                    Toast.makeText(this, "Failed to save meal data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}

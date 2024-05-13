package com.example.ezfitness

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class Category5Activity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private val meals = listOf(
        Meal("Butter Chicken", 500, 25, 20, 40),
        Meal("Palak Paneer", 350, 15, 10, 30),
        Meal("Chicken Biryani", 600, 30, 25, 50),
        Meal("Masala Dosa", 400, 10, 15, 35)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        findViewById<TextView>(R.id.textViewCategoryTitle).text = "Indian Cuisine"
        updateMealInformation()

        setupAddButtonListeners()
        setupBackButton()
    }

    private fun updateMealInformation() {
        // Updates the UI with the meal information
        findViewById<TextView>(R.id.meal1Name).text = meals[0].name
        findViewById<TextView>(R.id.meal1Calories).text = "Calories: ${meals[0].calories}"
        findViewById<TextView>(R.id.meal1Protein).text = "Protein: ${meals[0].protein}g"
        findViewById<TextView>(R.id.meal1Fat).text = "Fat: ${meals[0].fat}g"
        findViewById<TextView>(R.id.meal1Carbs).text = "Carbs: ${meals[0].carbs}g"

        findViewById<TextView>(R.id.meal2Name).text = meals[1].name
        findViewById<TextView>(R.id.meal2Calories).text = "Calories: ${meals[1].calories}"
        findViewById<TextView>(R.id.meal2Protein).text = "Protein: ${meals[1].protein}g"
        findViewById<TextView>(R.id.meal2Fat).text = "Fat: ${meals[1].fat}g"
        findViewById<TextView>(R.id.meal2Carbs).text = "Carbs: ${meals[1].carbs}g"

        findViewById<TextView>(R.id.meal3Name).text = meals[2].name
        findViewById<TextView>(R.id.meal3Calories).text = "Calories: ${meals[2].calories}"
        findViewById<TextView>(R.id.meal3Protein).text = "Protein: ${meals[2].protein}g"
        findViewById<TextView>(R.id.meal3Fat).text = "Fat: ${meals[2].fat}g"
        findViewById<TextView>(R.id.meal3Carbs).text = "Carbs: ${meals[2].carbs}g"

        findViewById<TextView>(R.id.meal4Name).text = meals[3].name
        findViewById<TextView>(R.id.meal4Calories).text = "Calories: ${meals[3].calories}"
        findViewById<TextView>(R.id.meal4Protein).text = "Protein: ${meals[3].protein}g"
        findViewById<TextView>(R.id.meal4Fat).text = "Fat: ${meals[3].fat}g"
        findViewById<TextView>(R.id.meal4Carbs).text = "Carbs: ${meals[3].carbs}g"
    }

    private fun saveMealToFirestore(meal: Meal) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            // Get current date
            val currentDate = Date()

            val mealMap = hashMapOf(
                "name" to meal.name,
                "calories" to meal.calories,
                "protein" to meal.protein,
                "fat" to meal.fat,
                "carbs" to meal.carbs,
                "dateAdded" to currentDate // Add the current date
            )

            db.collection("users").document(userId).collection("dietaryInformation")
                .add(mealMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "${meal.name} added to your dietary information", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add ${meal.name}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun setupAddButtonListeners() {
        findViewById<Button>(R.id.btnAddMeal1).setOnClickListener { saveMealToFirestore(meals[0]) }
        findViewById<Button>(R.id.btnAddMeal2).setOnClickListener { saveMealToFirestore(meals[1]) }
        findViewById<Button>(R.id.btnAddMeal3).setOnClickListener { saveMealToFirestore(meals[2]) }
        findViewById<Button>(R.id.btnAddMeal4).setOnClickListener { saveMealToFirestore(meals[3]) }
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    data class Meal(val name: String, val calories: Int, val protein: Int, val fat: Int, val carbs: Int)
}

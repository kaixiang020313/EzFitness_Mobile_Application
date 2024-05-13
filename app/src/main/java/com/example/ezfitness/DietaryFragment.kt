package com.example.ezfitness

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DietaryFragment : Fragment() {

    private lateinit var selectedDate: Calendar
    private lateinit var textViewDate: TextView
    private lateinit var btnPreviousDate: ImageButton
    private lateinit var btnNextDate: ImageButton
    private lateinit var btnAddMeal: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dietary, container, false)
        textViewDate = view.findViewById(R.id.textViewDate)
        btnPreviousDate = view.findViewById(R.id.btnPreviousDate)
        btnNextDate = view.findViewById(R.id.btnNextDate)
        btnAddMeal = view.findViewById(R.id.btnAddMeal)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize selected date to today
        selectedDate = Calendar.getInstance()

        // Set initial date text
        updateDateText(selectedDate)

        // Set click listener for date text to open date picker
        textViewDate.setOnClickListener {
            showDatePicker()
        }

        // Set click listeners for previous and next date buttons
        btnPreviousDate.setOnClickListener {
            previousDate()
        }

        btnNextDate.setOnClickListener {
            nextDate()
        }

        fetchBaseGoal()
        fetchMealsForSelectedDate()

        btnAddMeal.setOnClickListener{
            navigateToAddMeal()
        }
    }

    private fun updateDateText(date: Calendar) {
        val dateString = "${date.get(Calendar.YEAR)}-${date.get(Calendar.MONTH) + 1}-${date.get(Calendar.DAY_OF_MONTH)}"
        textViewDate.text = dateString
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
            updateDateText(selectedDate)
            fetchMealsForSelectedDate() // Ensure this is called after updating the date
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }


    private fun previousDate() {
        selectedDate.add(Calendar.DATE, -1)
        updateDateText(selectedDate)
        fetchMealsForSelectedDate()
    }

    private fun nextDate() {
        selectedDate.add(Calendar.DATE, 1)
        updateDateText(selectedDate)
        fetchMealsForSelectedDate()
    }


    private fun navigateToAddMeal(){
        val intent = Intent(activity, AddMealActivity::class.java)
        startActivity(intent)
    }

    private var baseGoal: Long = 0
    private fun fetchBaseGoal() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("baseGoal")) {
                        baseGoal = document.getLong("baseGoal") ?: 0L // Update baseGoal
                        view?.findViewById<TextView>(R.id.textViewCaloriesRemainingBaseGoal)?.text = "Consume Goal: $baseGoal kcal"
                        // Ensure to fetch meals after updating baseGoal
                        fetchMealsForSelectedDate()
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("DietaryFragment", "Error fetching baseGoal", e)
                }
        }
    }

    private fun fetchMealsForSelectedDate() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Start of the selected day
        val startOfDay = Calendar.getInstance().apply {
            time = selectedDate.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // End of the selected day
        val endOfDay = Calendar.getInstance().apply {
            time = selectedDate.time
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("dietaryInformation")
            .whereGreaterThanOrEqualTo("dateAdded", Timestamp(startOfDay))
            .whereLessThanOrEqualTo("dateAdded", Timestamp(endOfDay))
            .get()
            .addOnSuccessListener { documents ->
                var totalCalories = 0
                var totalProtein = 0
                var totalFat = 0
                var totalCarbs = 0

                for (document in documents) {
                    totalCalories += document.getLong("calories")?.toInt() ?: 0
                    totalProtein += document.getLong("protein")?.toInt() ?: 0
                    totalFat += document.getLong("fat")?.toInt() ?: 0
                    totalCarbs += document.getLong("carbs")?.toInt() ?: 0
                }

                updateNutritionConsumed(totalCalories, totalProtein, totalFat, totalCarbs)
            }
            .addOnFailureListener { e ->
                Log.w("DietaryFragment", "Error fetching meals", e)
            }
    }


    private fun updateNutritionConsumed(calories: Int, protein: Int, fat: Int, carbs: Int) {
        view?.findViewById<TextView>(R.id.textViewDateConsumedCalories)?.text = "Calories Consumed: $calories kcal"
        view?.findViewById<TextView>(R.id.textViewCaloriesRemainingFood)?.text = "Calories Consumed: $calories kcal"
        view?.findViewById<TextView>(R.id.textViewDateConsumedProtein)?.text = "Protein Consumed: ${protein}g"
        view?.findViewById<TextView>(R.id.textViewDateConsumedFat)?.text = "Fat Consumed: ${fat}g"
        view?.findViewById<TextView>(R.id.textViewDateConsumedCarbs)?.text = "Carbs Consumed: ${carbs}g"

        val remainingCalories = baseGoal - calories
        view?.findViewById<EditText>(R.id.editTextCaloriesRemaining)?.setText(remainingCalories.toString())
    }


}

package com.example.ezfitness

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.regex.Pattern

class HomeFragment : Fragment() {

    private lateinit var editTextWeight: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var buttonCalculateBMI: Button
    private lateinit var editTextWeightGoal: EditText
    private lateinit var spinnerSetGoal: Spinner
    private lateinit var buttonSetGoal: Button
    private lateinit var textViewBMIResult: TextView
    private lateinit var textViewCaloriesRemainingBaseGoal: TextView
    private lateinit var textViewCaloriesRemainingFood: TextView
    private lateinit var textViewCaloriesRemainingWorkout: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        editTextWeight = view.findViewById(R.id.editTextWeight)
        editTextHeight = view.findViewById(R.id.editTextHeight)
        buttonCalculateBMI = view.findViewById(R.id.buttonCalculateBMI)
        editTextWeightGoal = view.findViewById(R.id.editTextWeightGoal)
        spinnerSetGoal = view.findViewById(R.id.spinnerSetGoal)
        buttonSetGoal = view.findViewById(R.id.buttonSetGoal)
        textViewBMIResult = view.findViewById(R.id.textViewBMIResult)
        textViewCaloriesRemainingBaseGoal = view.findViewById(R.id.textViewCaloriesRemainingBaseGoal)
        textViewCaloriesRemainingFood = view.findViewById(R.id.textViewCaloriesRemainingFood)
        textViewCaloriesRemainingWorkout = view.findViewById(R.id.textViewCaloriesRemainingWorkout)

        editTextHeight.addValidation("Height", "^[0-9]+\$")
        editTextWeight.addValidation("Weight", "^[0-9]+\$")

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.goal_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSetGoal.adapter = adapter

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        userId?.let { fetchUserData(it) }

        buttonCalculateBMI.setOnClickListener {
            val weight = editTextWeight.text.toString().toFloatOrNull()
            val height = editTextHeight.text.toString().toFloatOrNull()

            if (weight != null && height != null) {
                calculateBMI(height, weight)
            } else {
                Toast.makeText(context, "Please enter valid weight and height", Toast.LENGTH_SHORT).show()
            }
        }

        buttonSetGoal.setOnClickListener {
            val weightGoal = editTextWeightGoal.text.toString().toFloatOrNull()
            val selectedGoal = spinnerSetGoal.selectedItem.toString()

            if (weightGoal != null) {
                setGoal(weightGoal, selectedGoal)
            } else {
                Toast.makeText(context, "Invalid weight goal", Toast.LENGTH_SHORT).show()
            }
        }

        fetchTotalCaloriesForCurrentDate()

        // 添加 Firebase Storage 的监听器以检测新的视频上传
        listenForVideoChanges()

        return view
    }

    override fun onStart() {
        super.onStart()
        // 监听视频上传和删除事件
        listenForVideoChanges()
    }

    private fun listenForVideoChanges() {
        val storageReference = FirebaseStorage.getInstance().reference
        val videoPaths = listOf("leg", "abs", "pushup", "tabata")

        // 添加监听器
        var totalCalories = 0
        videoPaths.forEach { videoPath ->
            val videoRef = storageReference.child(videoPath)
            videoRef.listAll()
                .addOnSuccessListener { listResult ->
                    // 视频上传事件
                    val caloriesForPath = listResult.items.size * getCaloriesForVideoPath(videoPath)
                    totalCalories += caloriesForPath
                    updateWorkoutCaloriesTextView(totalCalories)
                }
                .addOnFailureListener { exception ->
                    // 处理监听失败
                    Log.e(TAG, "Error getting list of items in $videoPath", exception)
                }
        }
    }

    private fun getCaloriesForVideoPath(videoPath: String): Int {
        // 根据视频路径返回相应的卡路里数
        return when (videoPath) {
            "leg" -> 125
            "abs" -> 150
            "pushup" -> 130
            "tabata" -> 150
            else -> 0
        }
    }

    private fun fetchUserData(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val height = document.getString("height") ?: ""
                    val weight = document.getString("weight") ?: ""
                    val weightGoal = document.getDouble("weightGoal")?.toFloat() ?: 0f
                    val selectedGoal = document.getString("selectedGoal") ?: ""
                    val dateOfBirth = document.getString("dateOfBirth")
                    val gender = document.getString("gender")
                    val activityLevel = document.getString("activityLevel")

                    editTextHeight.setText(height)
                    editTextWeight.setText(weight)
                    editTextWeightGoal.setText(weightGoal.toString())

                    val ageYears = calculateAge(dateOfBirth)
                    val isMale = determineGender(gender)
                    val initialHeight = height.toFloatOrNull() ?: 0f
                    val initialWeight = weight.toFloatOrNull() ?: 0f
                    calculateBMI(initialHeight, initialWeight)

                    val baseGoal = activityLevel?.let {
                        calculateBaseGoal(initialWeight, initialHeight, ageYears, isMale, it, selectedGoal)
                    }
                    textViewCaloriesRemainingBaseGoal.text = getString(R.string.base_goal_result, baseGoal)

                    val goalOptions = resources.getStringArray(R.array.goal_options)
                    val selectedGoalIndex = goalOptions.indexOf(selectedGoal)
                    if (selectedGoalIndex != -1) {
                        spinnerSetGoal.setSelection(selectedGoalIndex)
                    }
                }
            }
    }

    private fun fetchTotalCaloriesForCurrentDate() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("FetchCalories", "User ID is null. Make sure the user is logged in.")
            return
        }

        val currentDate = Calendar.getInstance()
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
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
                for (document in documents) {
                    totalCalories += document.getLong("calories")?.toInt() ?: 0
                }
                updateCaloriesConsumedTextView(totalCalories)
            }
            .addOnFailureListener { e ->
                Log.e("FetchCalories", "Error fetching meals: ", e)
            }
    }

    private fun calculateBMI(height: Float, weight: Float) {
        val bmi = weight / ((height / 100) * (height / 100))
        val bmiResultText = getString(R.string.bmi_result, bmi)
        textViewBMIResult.text = bmiResultText
        updateBMIInFirestore(bmi)
    }

    private fun calculateBaseGoal(weight: Float, height: Float, ageYears: Int, isMale: Boolean, activityLevel: String, selectedGoal: String): Float {
        val bmr = if (isMale) {
            66.47f + (13.75f * weight) + (5.003f * height) - (6.755f * ageYears)
        } else {
            655.1f + (9.563f * weight) + (1.85f * height) - (4.676f * ageYears)
        }

        val baseGoal = when (activityLevel) {
            "Sedentary" -> bmr * 1.2f
            "Light Activity" -> bmr * 1.375f
            "Moderately Active" -> bmr * 1.55f
            "Very Active" -> bmr * 1.725f
            else -> bmr * 1.9f
        }

        val adjustedGoal = when (selectedGoal) {
            "Cut" -> baseGoal * 0.9f
            "Bulk" -> baseGoal * 1.1f
            else -> baseGoal
        }

        updateBaseGoalInFirestore(adjustedGoal)
        return adjustedGoal
    }

    private fun setGoal(weightGoal: Float?, selectedGoal: String) {
        val message = "Goal set successfully! Weight goal: $weightGoal, Selected goal: $selectedGoal"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        saveGoalInfoToFirestore(weightGoal, selectedGoal)
    }

    private fun updateBMIInFirestore(bmi: Float) {
        val userId = auth.currentUser?.uid
        userId?.let {
            val userRef = firestore.collection("users").document(it)
            userRef.update("bmi", bmi)
        }
    }

    private fun updateBaseGoalInFirestore(baseGoal: Float) {
        val userId = auth.currentUser?.uid
        userId?.let {
            val userRef = firestore.collection("users").document(it)
            userRef.update("baseGoal", baseGoal)
        }
    }

    private fun saveGoalInfoToFirestore(weightGoal: Float?, selectedGoal: String) {
        val userId = auth.currentUser?.uid
        userId?.let {
            val userRef = firestore.collection("users").document(it)
            val goalData = hashMapOf(
                "weightGoal" to weightGoal,
                "selectedGoal" to selectedGoal
            )
            userRef.update(goalData as Map<String, Any>)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating goal information", e)
                }
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

    private fun calculateAge(dateOfBirth: String?): Int {
        if (dateOfBirth.isNullOrEmpty()) return 0

        val dobParts = dateOfBirth.split("-")
        if (dobParts.size != 3) return 0

        val dobYear = dobParts[0].toInt()
        val dobMonth = dobParts[1].toInt()
        val dobDay = dobParts[2].toInt()

        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance()
        birthDate.set(dobYear, dobMonth - 1, dobDay)

        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun determineGender(gender: String?): Boolean {
        return gender.equals("Male", ignoreCase = true)
    }

    private fun updateCaloriesConsumedTextView(totalCalories: Int) {
        textViewCaloriesRemainingFood.text = "Food: $totalCalories kcal"
    }

    private fun updateWorkoutCaloriesTextView(totalCalories: Int) {
        val newText = "Workout: $totalCalories cal"
        textViewCaloriesRemainingWorkout.text = newText
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}

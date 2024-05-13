package com.example.ezfitness

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var imageButtonEditProfile: ImageButton
    private lateinit var buttonLogOut: Button
    private lateinit var buttonScan: Button

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var workoutTimeTextView: TextView
    private lateinit var caloriesBurnedTextView: TextView
    private lateinit var workoutsDoneTextView: TextView
    private lateinit var imageViewProfile: ImageView


    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageButtonEditProfile = view.findViewById(R.id.imageButtonEditProfile)
        buttonLogOut = view.findViewById(R.id.buttonLogOut)
        buttonScan = view.findViewById(R.id.buttonScan)

        usernameTextView = view.findViewById(R.id.textViewEW)
        emailTextView = view.findViewById(R.id.textViewEmail)
        workoutTimeTextView = view.findViewById(R.id.textViewWorkoutTime)
        caloriesBurnedTextView = view.findViewById(R.id.textViewCaloriesBurned)
        workoutsDoneTextView = view.findViewById(R.id.textViewWorkoutsDone)
        imageViewProfile = view.findViewById(R.id.imageViewProfile)

        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            emailTextView.text = user.email ?: "No Email"

            fetchUserData(user.uid)
        }



        buttonLogOut.setOnClickListener {
            logOutUser()
        }

        buttonScan.setOnClickListener {
            navigateToInbodyReport()
        }

        imageButtonEditProfile.setOnClickListener {
            navigateToEditProfile()
        }



    }

    private fun fetchUserData(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "No Username"
                    val workoutTime = document.getString("workoutTime") ?: "N/A"
                    val caloriesBurned = document.getString("caloriesBurned") ?: "N/A"
                    val workoutsDone = document.getString("workoutsDone") ?: "N/A"
                    val profilePictureUrl = document.getString("profilePictureUrl")

                    usernameTextView.text = username
                    workoutTimeTextView.text = workoutTime
                    caloriesBurnedTextView.text = caloriesBurned
                    workoutsDoneTextView.text = workoutsDone

                    // Check if profilePictureUrl is not null
                    if (!profilePictureUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profilePictureUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .into(imageViewProfile)
                    } else {
                        Log.e("ProfileFragment", "Profile picture URL is null or empty")
                    }
                } else {
                    Log.e("ProfileFragment", "Document does not exist or is null")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Failed to fetch user data: ${exception.message}")
                // Handle failure
            }
    }



    private fun logOutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun navigateToInbodyReport() {
        val intent = Intent(activity, InbodyReport::class.java)
        startActivity(intent)
    }

    private fun navigateToEditProfile() {
        val intent = Intent(activity, EditProfileActivity::class.java)
        startActivity(intent)
    }


}

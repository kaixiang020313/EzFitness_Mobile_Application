package com.example.ezfitness

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AddMealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)

        // Find views
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val btnNewMeal: Button = findViewById(R.id.btnNewMeal)
        val btnFoodScanner: Button = findViewById(R.id.btnFoodScanner)
        val btnCategory1: Button = findViewById(R.id.btnCategory1)
        val btnCategory2: Button = findViewById(R.id.btnCategory2)
        val btnCategory3: Button = findViewById(R.id.btnCategory3)
        val btnCategory4: Button = findViewById(R.id.btnCategory4)
        val btnCategory5: Button = findViewById(R.id.btnCategory5)

        // Set click listeners
        btnBack.setOnClickListener {
            finish() // Finish the activity and go back
        }

        btnNewMeal.setOnClickListener {
            // Navigate to the New Meal page
            val intent = Intent(this, NewMealActivity::class.java)
            startActivity(intent)
        }

        btnCategory1.setOnClickListener {
            val intent = Intent(this, Category1Activity::class.java)
            startActivity(intent)
        }

        btnCategory2.setOnClickListener {
            val intent = Intent(this, Category2Activity::class.java)
            startActivity(intent)
        }

        btnCategory3.setOnClickListener {
            val intent = Intent(this, Category3Activity::class.java)
            startActivity(intent)
        }

        btnCategory4.setOnClickListener {
            val intent = Intent(this, Category4Activity::class.java)
            startActivity(intent)
        }

        btnCategory5.setOnClickListener {
            val intent = Intent(this, Category5Activity::class.java)
            startActivity(intent)
        }

        btnFoodScanner.setOnClickListener {
            // Create an Intent to open the gallery
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            // Create an Intent to open Google Drive
            val driveIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            driveIntent.addCategory(Intent.CATEGORY_OPENABLE)
            driveIntent.type = "image/*"

            // Create a chooser for both intents
            val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(driveIntent))

            // Start the activity
            val REQUEST_SELECT_IMAGE = 0
            startActivityForResult(chooserIntent, REQUEST_SELECT_IMAGE)
        }


    }
}

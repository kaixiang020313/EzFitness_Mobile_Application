package com.example.ezfitness

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.menu_workout -> {
                    replaceFragment(WorkoutFragment())
                    true
                }
                R.id.menu_dietary -> {
                    replaceFragment(DietaryFragment())
                    true
                }
                R.id.menu_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        val destinationFragment = intent.getIntExtra("destinationFragment", -1)
        if (destinationFragment != -1) {
            when (destinationFragment) {
                R.id.menu_workout -> bottomNavigationView.selectedItemId = R.id.menu_workout
                R.id.menu_dietary -> bottomNavigationView.selectedItemId = R.id.menu_dietary
                R.id.menu_profile -> bottomNavigationView.selectedItemId = R.id.menu_profile
                else -> bottomNavigationView.selectedItemId = R.id.menu_home
            }
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}

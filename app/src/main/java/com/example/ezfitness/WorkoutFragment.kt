package com.example.ezfitness

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView


class WorkoutFragment : Fragment() {

    private lateinit var imageSquat: ImageView
    private lateinit var iBoxing: ImageView
    private lateinit var imageLeg: ImageView
    private lateinit var imagePushUp: ImageView
    private lateinit var BoxingButton: Button
    private lateinit var HiitButton: Button
    private lateinit var YogaButton: Button
    private lateinit var PilatesButton: Button
    private lateinit var buttonPlanner: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageSquat = view.findViewById(R.id.imageSquat)
        iBoxing = view.findViewById(R.id.iBoxing)
        imageLeg = view.findViewById(R.id.imageLeg)
        imagePushUp = view.findViewById(R.id.imagePushUp)
        BoxingButton = view.findViewById(R.id.BoxingButton)
        HiitButton = view.findViewById(R.id.HiitButton)
        YogaButton = view.findViewById(R.id.YogaButton)
        PilatesButton = view.findViewById(R.id.PilatesButton)
        buttonPlanner = view.findViewById(R.id.buttonPlanner)

        imageSquat.setOnClickListener {
            navigateToWorkUpPreview()
        }

        iBoxing.setOnClickListener {
            navigateToWorkUpPreview2()
        }

        imageLeg.setOnClickListener {
            navigateToWorkUpPreview3()
        }

        imagePushUp.setOnClickListener {
            navigateToWorkUpPreview4()
        }

        BoxingButton.setOnClickListener {
            navigateToBoxing()
        }

        HiitButton.setOnClickListener {
            navigateToHiit()
        }

        YogaButton.setOnClickListener {
            navigateToYoga()
        }

        PilatesButton.setOnClickListener {
            navigateToPilates()
        }

        buttonPlanner.setOnClickListener {
            navigateToPlanner()
        }
    }

    private fun navigateToWorkUpPreview() {
        val intent = Intent(activity, WorkUpPreview::class.java)
        startActivity(intent)
    }

    private fun navigateToWorkUpPreview2() {
        val intent = Intent(activity, WorkUpPreview2::class.java)
        startActivity(intent)
    }

    private fun navigateToWorkUpPreview3() {
        val intent = Intent(activity, WorkUpPreview3::class.java)
        startActivity(intent)
    }

    private fun navigateToWorkUpPreview4() {
        val intent = Intent(activity, WorkUpPreview4::class.java)
        startActivity(intent)
    }

    private fun navigateToBoxing() {
        val intent = Intent(activity, Boxing::class.java)
        startActivity(intent)
    }

    private fun navigateToHiit() {
        val intent = Intent(activity, Hiit::class.java)
        startActivity(intent)
    }

    private fun navigateToYoga() {
        val intent = Intent(activity, Yoga::class.java)
        startActivity(intent)
    }

    private fun navigateToPilates() {
        val intent = Intent(activity, Pilates::class.java)
        startActivity(intent)
    }

    private fun navigateToPlanner() {
        val intent = Intent(activity, Planner::class.java)
        startActivity(intent)
    }
}
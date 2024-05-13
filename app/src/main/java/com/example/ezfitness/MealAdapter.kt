package com.example.ezfitness

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MealAdapter constructor(private var meals: List<Meal>) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewMealName: TextView = view.findViewById(R.id.textViewMealName)
        val textViewMealCategory: TextView = view.findViewById(R.id.textViewMealName)
        var textViewMealCalories: TextView = view.findViewById(R.id.textViewMealName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.textViewMealName.text = meal.name
        holder.textViewMealCategory.text = meal.category
    }

    override fun getItemCount() = meals.size

    fun updateMeals(meals: List<Meal>) {
        this.meals = meals
        notifyDataSetChanged()
    }
}

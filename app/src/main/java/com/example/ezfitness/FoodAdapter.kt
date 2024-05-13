package com.example.ezfitness

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(private val foodList: List<FoodItem>) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewFoodName)
        val categoryTextView: TextView = itemView.findViewById(R.id.textViewFoodCategory)
        val caloriesTextView: TextView = itemView.findViewById(R.id.textViewFoodCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val currentItem = foodList[position]
        holder.nameTextView.text = currentItem.name
        holder.categoryTextView.text = currentItem.category
        holder.caloriesTextView.text = currentItem.calories.toString()
    }

    override fun getItemCount() = foodList.size
}

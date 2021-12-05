package com.example.achievmaps.achievementsScreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.achievmaps.R

class AchievementsAdapter(private val achievementsPage: ArrayList<ArrayList<String>>) :
    RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val achievementName = itemView.findViewById<TextView>(R.id.AchievementItemName)
        val achievementDescription = itemView.findViewById<TextView>(R.id.AchievementItemDescription)
        val achievementPoints = itemView.findViewById<TextView>(R.id.AchievementItemPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementsAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.achievements_item_layout, parent, false)
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: AchievementsAdapter.ViewHolder, position: Int) {
        val data: ArrayList<String> = achievementsPage.get(position)
        viewHolder.achievementName.text = data[0]
        viewHolder.achievementDescription.text = data[1]
        viewHolder.achievementPoints.text = data[2]
    }

    override fun getItemCount(): Int {
        return achievementsPage.size
    }
}
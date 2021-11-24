package com.example.achievmaps.RankingScreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.achievmaps.R

class RankingAdapter (private val rankingPage: ArrayList<ArrayList<String>>) : RecyclerView.Adapter<RankingAdapter.ViewHolder>()
{
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankField = itemView.findViewById<TextView>(R.id.Rank)
        val nicknameField = itemView.findViewById<TextView>(R.id.Nickname)
        val pointsField = itemView.findViewById<TextView>(R.id.Points)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.ranking_item_layout, parent, false)
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: RankingAdapter.ViewHolder, position: Int) {
        val data: ArrayList<String> = rankingPage.get(position)
        viewHolder.rankField.setText(data[0])
        viewHolder.nicknameField.setText(data[1])
        viewHolder.pointsField.setText(data[2])
    }

    override fun getItemCount(): Int {
        return rankingPage.size
    }
}
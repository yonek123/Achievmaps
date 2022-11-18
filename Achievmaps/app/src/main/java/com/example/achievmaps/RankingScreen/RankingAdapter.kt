package com.example.achievmaps.RankingScreen

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.achievmaps.loginScreen.LoginScreen
import com.example.achievmaps.R

class RankingAdapter(private val rankingPage: ArrayList<ArrayList<String>>) :
    RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankField: TextView = itemView.findViewById(R.id.RankingRank)
        val nicknameField: TextView = itemView.findViewById(R.id.RankingNickname)
        val pointsField: TextView = itemView.findViewById(R.id.RankingPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.ranking_item_layout, parent, false)
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data: ArrayList<String> = rankingPage[position]
        if (data[1] == LoginScreen.loggedUserNick) {
            viewHolder.rankField.setTextColor(Color.RED)
            viewHolder.nicknameField.setTextColor(Color.RED)
            viewHolder.pointsField.setTextColor(Color.RED)
        }
        else {
            viewHolder.rankField.setTextColor(Color.BLACK)
            viewHolder.nicknameField.setTextColor(Color.BLACK)
            viewHolder.pointsField.setTextColor(Color.BLACK)
        }
        viewHolder.rankField.text = data[0]
        viewHolder.nicknameField.text = data[1]
        viewHolder.pointsField.text = data[2]
    }

    override fun getItemCount(): Int {
        return rankingPage.size
    }
}
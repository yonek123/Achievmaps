package com.example.achievmaps.friendsScreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.achievmaps.R

class FriendsAdapter(private val friendsPage: ArrayList<ArrayList<String>>) :
    RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendNickname = itemView.findViewById<TextView>(R.id.FriendsNickname)
        val friendPointsAll = itemView.findViewById<TextView>(R.id.FriendsPointsAll)
        val friendPointsNature = itemView.findViewById<TextView>(R.id.FriendsPointsNature)
        val friendPointsArchitecture =
            itemView.findViewById<TextView>(R.id.FriendsPointsArchitecture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.friends_item_layout, parent, false)
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: FriendsAdapter.ViewHolder, position: Int) {
        val data: ArrayList<String> = friendsPage.get(position)
        viewHolder.friendNickname.text = data[0]
        viewHolder.friendPointsAll.text = data[1]
        viewHolder.friendPointsNature.text = data[2]
        viewHolder.friendPointsArchitecture.text = data[3]
    }

    override fun getItemCount(): Int {
        return friendsPage.size
    }
}
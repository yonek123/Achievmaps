package com.example.achievmaps.mapScreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.achievmaps.R

class TransitAdapter(private val transitPage: ArrayList<ArrayList<String>>) :
    RecyclerView.Adapter<TransitAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val startName: TextView = itemView.findViewById(R.id.StartName)
        val transitName: TextView = itemView.findViewById(R.id.TransitName)
        val endName: TextView = itemView.findViewById(R.id.EndName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransitAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.transit_item_layout, parent, false)
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: TransitAdapter.ViewHolder, position: Int) {
        val data: ArrayList<String> = transitPage[position]
        viewHolder.startName.text = data[0]
        viewHolder.transitName.text = data[1]
        viewHolder.endName.text = data[2]
        println(data[2])
    }

    override fun getItemCount(): Int {
        return transitPage.size
    }
}
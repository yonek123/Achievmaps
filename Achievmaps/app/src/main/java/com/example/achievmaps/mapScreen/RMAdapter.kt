package com.example.achievmaps.mapScreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.achievmaps.R

class RMAdapter(private val arrayList: ArrayList<TagObj>) :
    RecyclerView.Adapter<RMAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RMAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.rm_item_layout, parent, false)
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rmObject = arrayList[position]
        holder.textView.text = rmObject.name
        holder.checkBox.isChecked = rmObject.isSelected
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView
        var checkBox: CheckBox

        init {
            textView = itemView.findViewById(R.id.RMName)
            checkBox = itemView.findViewById(R.id.RMCheckBox)
            checkBox.setOnClickListener { v ->
                val isChecked = (v as CheckBox).isChecked
                arrayList[adapterPosition].isSelected = isChecked
                notifyDataSetChanged()
            }
        }
    }
}
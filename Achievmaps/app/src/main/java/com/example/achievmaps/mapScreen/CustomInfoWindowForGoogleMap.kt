package com.example.achievmaps.mapScreen

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.achievmaps.R
import com.example.achievmaps.databaseConnections.MapDB
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.friends_screen.*

class CustomInfoWindowForGoogleMap(context: Context) : GoogleMap.InfoWindowAdapter {

    var mContext = context
    var mWindow = (context as Activity).layoutInflater.inflate(R.layout.activity_maps_info_box, null)

    private fun rendowWindowText(marker: Marker, view: View){

        val tvTitle = view.findViewById<TextView>(R.id.title)
        val tvSnippet = view.findViewById<TextView>(R.id.snippet)
        val tvButton = view.findViewById<Button>(R.id.infoWindowButton)

        tvTitle.text = marker.title
        tvSnippet.text = marker.snippet
        tvButton.setOnClickListener(View.OnClickListener {
            //MapDB.addAchievement()
        })
    }

    override fun getInfoContents(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoWindow(marker: Marker): View? {
        rendowWindowText(marker, mWindow)
        return mWindow
    }
}
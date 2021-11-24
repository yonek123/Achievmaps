package com.example.achievmaps.RankingScreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.achievmaps.LoginScreen.LoginScreen
import com.example.achievmaps.MainMenuScreen.MainMenuScreen
import com.example.achievmaps.R
import kotlinx.android.synthetic.main.ranking_screen.*

class RankingScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var row = ArrayList<String>()
        var table = ArrayList<ArrayList<String>>()
        var poz = 0
        for (item in MainMenuScreen.lines) {
            row.add(item)
            poz++
            if (poz > 2) {
                poz = 0
                table.add(row.clone() as ArrayList<String>)
                row.clear()
            }
        }

        setContentView(R.layout.ranking_screen)

        val adapter = RankingAdapter(table)
        RankingView.adapter = adapter
        RankingView.layoutManager = LinearLayoutManager(this)
        RankingView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
}
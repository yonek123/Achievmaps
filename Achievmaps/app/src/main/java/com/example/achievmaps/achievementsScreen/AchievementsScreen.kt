package com.example.achievmaps.achievementsScreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.achievmaps.R
import com.example.achievmaps.databaseConnections.DatabaseConnections
import com.example.achievmaps.loginScreen.LoginScreen
import kotlinx.android.synthetic.main.achievements_screen.*

@Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNCHECKED_CAST", "UNUSED_PARAMETER")
class AchievementsScreen : AppCompatActivity() {
    private var list = listOf("0")
    private var row = ArrayList<String>()
    private var table = ArrayList<ArrayList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.achievements_screen)
        loadAchievements()
    }

    private fun loadAchievements() {
        AchievementsView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        AchievementsLoadingScreen.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            var achievementsData = "-3"
            val t = Thread {
                achievementsData = DatabaseConnections.getTables(
                    getString(R.string.url_text) + "getAchievements.php?nickname="
                            + LoginScreen.loggedUserNick
                )
                list = achievementsData.split('\n')
            }
            t.start()
            t.join()

            if (list[0] == "-3") {
                AchievementsErrorText.text = getString(R.string.database_conn_error3_text)
                AchievementsErrorLayout.visibility = View.VISIBLE
                AchievementsLoadingScreen.visibility = View.GONE
            } else if (list[0] == "-2") {
                AchievementsErrorText.text = getString(R.string.database_conn_error2_text)
                AchievementsErrorLayout.visibility = View.VISIBLE
                AchievementsLoadingScreen.visibility = View.GONE
            } else {
                row.clear()
                table.clear()
                var poz = 0
                for (item in list) {
                    row.add(item)
                    poz++
                    if (poz > 2) {
                        poz = 0
                        table.add(row.clone() as ArrayList<String>)
                        row.clear()
                    }
                }

                AchievementsView.swapAdapter(AchievementsAdapter(table), true)
                AchievementsView.layoutManager = LinearLayoutManager(this)
                AchievementsLoadingScreen.visibility = View.GONE
            }
        }, 100)
    }

    fun closeAchievementsErrorLayout(view: View) {
        AchievementsErrorLayout.visibility = View.GONE
        this.finish()
    }
}
package com.example.achievmaps.mainMenuScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.achievmaps.loginScreen.LoginScreen
import com.example.achievmaps.R
import com.example.achievmaps.rankingScreen.RankingScreen
import kotlinx.android.synthetic.main.main_menu_screen.*

class MainMenuScreen : AppCompatActivity() {

    companion object {
        var lines = listOf("0")
    }

    override fun onResume() {
        super.onResume()
        MainMenuLoadingScreen.visibility = View.GONE
        setMainMenuEnabled(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu_screen)
        MenuScreenText.text = "Witaj " + LoginScreen.loggedUserNick + "!"
    }

    fun setMainMenuEnabled(setting: Boolean) {
        MainMenuMapSubmenu.isEnabled = setting
        MainMenuNewsSubmenu.isEnabled = setting
        MainMenuAchievementsSubmenu.isEnabled = setting
        MainMenuFriendsSubmenu.isEnabled = setting
        MainMenuRankingSubmenu.isEnabled = setting
    }

    fun closeMainMenuErrorLayout(view: View) {
        MainMenuErrorLayout.visibility = View.GONE
        setMainMenuEnabled(true)
    }

    fun openMap(view: View) {
        MainMenuLoadingScreen.visibility = View.VISIBLE
        setMainMenuEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            MainMenuLoadingScreen.visibility = View.GONE
            setMainMenuEnabled(true)
        }, 1000)
    }

    fun openNews(view: View) {
        MainMenuLoadingScreen.visibility = View.VISIBLE
        setMainMenuEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            MainMenuLoadingScreen.visibility = View.GONE
            setMainMenuEnabled(true)
        }, 1000)
    }

    fun openAchievements(view: View) {
        MainMenuLoadingScreen.visibility = View.VISIBLE
        setMainMenuEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            MainMenuLoadingScreen.visibility = View.GONE
            setMainMenuEnabled(true)
        }, 1000)
    }

    fun openFriends(view: View) {
        MainMenuLoadingScreen.visibility = View.VISIBLE
        setMainMenuEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            MainMenuLoadingScreen.visibility = View.GONE
            setMainMenuEnabled(true)
        }, 1000)
    }

    fun openRanking(view: View) {
        MainMenuLoadingScreen.visibility = View.VISIBLE
        setMainMenuEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, RankingScreen::class.java)
            startActivity(intent)
        }, 1000)
    }
}
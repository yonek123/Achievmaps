package com.example.achievmaps.mainMenuScreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.achievmaps.R
import com.example.achievmaps.achievementsScreen.AchievementsScreen
import com.example.achievmaps.friendsScreen.FriendsScreen
import com.example.achievmaps.loginScreen.LoginScreen
import com.example.achievmaps.mapScreen.MapScreen
import com.example.achievmaps.newsScreen.NewsScreen
import com.example.achievmaps.rankingScreen.RankingScreen
import kotlinx.android.synthetic.main.main_menu_screen.*

@Suppress("UNUSED_PARAMETER")
class MainMenuScreen : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        MainMenuLoadingScreen.visibility = View.GONE
        setMainMenuEnabled(true)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu_screen)
        MenuScreenText.text = "Witaj " + LoginScreen.loggedUserNick + "!"
    }

    private fun setMainMenuEnabled(setting: Boolean) {
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
            val intent = Intent(this, MapScreen::class.java)
            startActivity(intent)
        }, 100)
    }

    fun openNews(view: View) {
        MainMenuLoadingScreen.visibility = View.VISIBLE
        setMainMenuEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, NewsScreen::class.java)
            startActivity(intent)
        }, 100)
    }

    fun openAchievements(view: View) {
        MainMenuLoadingScreen.visibility = View.VISIBLE
        setMainMenuEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, AchievementsScreen::class.java)
            startActivity(intent)
        }, 100)
    }

    fun openFriends(view: View) {
        MainMenuLoadingScreen.visibility = View.VISIBLE
        setMainMenuEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, FriendsScreen::class.java)
            startActivity(intent)
        }, 100)
    }

    fun openRanking(view: View) {
        MainMenuLoadingScreen.visibility = View.VISIBLE
        setMainMenuEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, RankingScreen::class.java)
            startActivity(intent)
        }, 100)
    }
}
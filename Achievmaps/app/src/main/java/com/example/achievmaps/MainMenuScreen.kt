package com.example.achievmaps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.achievmaps.LoginScreen.LoginScreen
import kotlinx.android.synthetic.main.main_menu_screen.*

class MainMenuScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu_screen)
        MenuScreenText.text = "Witaj " + LoginScreen.loggedUserNick + "!"
    }
}
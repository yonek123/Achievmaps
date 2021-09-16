package com.example.achievmaps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.content.Context
import kotlinx.android.synthetic.main.activity_login_screen.*


class LoginScreen : AppCompatActivity() {
    private val sharedPrefFile = "AchievmapsPref"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        val sharedPref = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("isSaved", false)) {
            System.err.println("3")
            LoginLoginField.setText(sharedPref.getString("login", "Login"))
            LoginPasswordField.setText(sharedPref.getString("password", "Hasło"))
        }
    }

    fun loginCompleted(view: View) {
        val sharedPref = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        if (LoginRememberMe.isChecked) {
            System.err.println("1")
            editor.putString("login", LoginLoginField.text.toString())
            editor.putString("password", LoginPasswordField.text.toString())
            editor.putBoolean("isSaved", true)
            editor.commit()
        } else {
            System.err.println("2")
            editor.putString("login", "")
            editor.putString("password", "")
            editor.putBoolean("isSaved", false)
            editor.commit()
        }

        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    fun visibleRegister(view: View) {
        LoginLoginField.setVisibility(View.GONE)
        LoginPasswordField.setVisibility(View.GONE)
        LoginRememberMe.setVisibility(View.GONE)
        LoginLoginButton.setVisibility(View.GONE)
        LoginRegisterButton.setVisibility(View.GONE)
        RegisterEmailField.setVisibility(View.VISIBLE)
        RegisterLoginField.setVisibility(View.VISIBLE)
        RegisterPasswordField.setVisibility(View.VISIBLE)
        RegisterRepeatPasswordField.setVisibility(View.VISIBLE)
        RegisterNextButton.setVisibility(View.VISIBLE)
        RegisterBackButton.setVisibility(View.VISIBLE)
    }

    fun visibleLogin(view: View) {
        LoginLoginField.setVisibility(View.VISIBLE)
        LoginPasswordField.setVisibility(View.VISIBLE)
        LoginRememberMe.setVisibility(View.VISIBLE)
        LoginLoginButton.setVisibility(View.VISIBLE)
        LoginRegisterButton.setVisibility(View.VISIBLE)
        RegisterEmailField.setVisibility(View.GONE)
        RegisterLoginField.setVisibility(View.GONE)
        RegisterPasswordField.setVisibility(View.GONE)
        RegisterRepeatPasswordField.setVisibility(View.GONE)
        RegisterNextButton.setVisibility(View.GONE)
        RegisterBackButton.setVisibility(View.GONE)
    }
}
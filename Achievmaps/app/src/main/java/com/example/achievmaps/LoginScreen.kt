package com.example.achievmaps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class LoginScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
    }
    fun openMap(view: View) {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }
}
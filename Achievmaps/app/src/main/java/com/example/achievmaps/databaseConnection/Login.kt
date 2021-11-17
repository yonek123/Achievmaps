package com.example.achievmaps.databaseConnection

import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

object Login {
    fun login(email: String, password: String): Int {
        var data = -3
        val url =
            URL("https://justsomephp.000webhostapp.com/login.php?email=" + email + "&password=" + password)
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = url.openConnection() as HttpURLConnection
            data = urlConnection.inputStream.bufferedReader().readText().toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
        }
        return data
    }
}
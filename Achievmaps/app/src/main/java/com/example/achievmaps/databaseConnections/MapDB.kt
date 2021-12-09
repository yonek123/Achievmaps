package com.example.achievmaps.databaseConnections

import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


object MapDB {
    fun getMap(nickname: String): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/getMap.php?nickname=" + nickname)
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = url.openConnection() as HttpURLConnection
            data = urlConnection.inputStream.bufferedReader().readText()
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
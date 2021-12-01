package com.example.achievmaps.databaseConnections

import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

object Friends {
    fun getFriends(personid: Int): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/getFriends.php?personid=" + personid.toString())
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

    fun addFriend(personid: Int, friendNick: String): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/addFriend.php?personid=" + personid.toString() + "&friendnick=" + friendNick)
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

    fun deleteFriend(personid: Int, friendNick: String): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/deleteFriend.php?personid=" + personid.toString() + "&friendnick=" + friendNick)
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
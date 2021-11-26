package com.example.achievmaps.databaseConnections

import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

object Ranking {
    fun getByUser(personid: Int, type: String): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/getRankingByUser.php?personid=" + personid.toString() + "&type=" + type)
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

    fun getByPage(page: Int, type: String): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/getRankingByPage.php?page=" + page.toString() + "&type=" + type)
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

    fun getUserPage(personid: Int, type: String): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/getUserPage.php?personid=" + personid.toString() + "&type=" + type)
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

    fun getMaxPage(): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/getMaxPage.php")
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
package com.example.achievmaps.databaseConnections

import android.graphics.Bitmap
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.InputStream


object News {
    fun loadPage(): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/getNewsFirstPage.php")
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

    fun getNewsText(page: Int): String {
        var data = "-3"
        val url =
            URL("https://justsomephp.000webhostapp.com/getNews.php?page=" + page.toString())
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

    fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
            bm, 0, 0, width, height,
            matrix, false
        )
    }

    fun getNewsImage(page: Int): Bitmap? {
        var bitmap: Bitmap? = null
        val url =
            URL("https://justsomephp.000webhostapp.com/getNewsImage.php?page=" + page.toString())
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = url.openConnection() as HttpURLConnection
            val input: InputStream = urlConnection.getInputStream()
            bitmap = BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
        }
        if(bitmap != null)
            return getResizedBitmap(bitmap, 1080, 1920)
        else
            return null
    }
}
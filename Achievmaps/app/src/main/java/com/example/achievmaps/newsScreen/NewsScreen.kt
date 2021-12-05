package com.example.achievmaps.newsScreen

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.achievmaps.R
import com.example.achievmaps.databaseConnections.News
import kotlinx.android.synthetic.main.news_screen.*

class NewsScreen : AppCompatActivity() {
    private var page = 0
    private var maxpage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.news_screen)
        loadMaxPage()
    }

    fun setNewsEnabled(setting: Boolean) {
        NewsFirstPage.isEnabled = setting
        NewsNextPage.isEnabled = setting
        NewsPreviousPage.isEnabled = setting
    }

    fun loadMaxPage() {
        NewsLoadingScreen.visibility = View.VISIBLE
        setNewsEnabled(false)

        Handler(Looper.getMainLooper()).postDelayed({
            var newsData = "-3"
            var lines = listOf("0")
            val t = Thread {
                newsData =
                    News.loadPage()
                lines = newsData.split('\n')
            }
            t.start()
            t.join()

            if (lines[0] == "-3") {
                NewsErrorText.text = getString(R.string.database_conn_error3_text)
                NewsErrorLayout.visibility = View.VISIBLE
                NewsLoadingScreen.visibility = View.GONE
            } else if (lines[0] == "-2") {
                NewsErrorText.text = getString(R.string.database_conn_error2_text)
                NewsErrorLayout.visibility = View.VISIBLE
                NewsLoadingScreen.visibility = View.GONE
            } else {
                page = lines[0].toInt()
                maxpage = lines[0].toInt()
            }
            loadNews()
        }, 100)
    }

    fun loadNews() {
        NewsLoadingScreen.visibility = View.VISIBLE
        setNewsEnabled(false)

        Handler(Looper.getMainLooper()).postDelayed({
            var bitmap: Bitmap? = null
            var newsData = "-3"
            var lines = listOf("0")
            val t = Thread {
                newsData =
                    News.getNewsText(page)
                lines = newsData.split('\n')

                bitmap =
                    News.getNewsImage(page)
            }
            t.start()
            t.join()

            if (lines[0] == "-3") {
                NewsErrorText.text = getString(R.string.database_conn_error3_text)
                NewsErrorLayout.visibility = View.VISIBLE
                NewsLoadingScreen.visibility = View.GONE
            } else if (lines[0] == "-2" || bitmap == null) {
                NewsErrorText.text = getString(R.string.database_conn_error2_text)
                NewsErrorLayout.visibility = View.VISIBLE
                NewsLoadingScreen.visibility = View.GONE
            } else {
                var firstLine = true
                NewsText.text = ""
                for (l in lines) {
                    if (firstLine) {
                        NewsTitle.text = lines[0]
                        firstLine = false
                        continue
                    }
                    NewsText.text = NewsText.text.toString() + "\n" + l
                }
                NewsImage.setImageBitmap(bitmap)

                NewsLoadingScreen.visibility = View.GONE
                setNewsEnabled(true)
                Scroll.visibility = View.VISIBLE
                if (page == maxpage) {
                    NewsFirstPage.isEnabled = false
                    NewsNextPage.isEnabled = false
                    NewsFirstPage.setBackgroundColor(getColor(R.color.button_grayishgreen))
                    NewsNextPage.setBackgroundColor(getColor(R.color.button_grayishgreen))
                } else {
                    NewsFirstPage.setBackgroundColor(getColor(R.color.button_green))
                    NewsNextPage.setBackgroundColor(getColor(R.color.button_green))
                }
                if (page == 1) {
                    NewsPreviousPage.isEnabled = false
                    NewsPreviousPage.setBackgroundColor(getColor(R.color.button_grayishgreen))
                } else {
                    NewsPreviousPage.setBackgroundColor(getColor(R.color.button_green))
                }
            }
        }, 100)
    }

    fun closeNewsErrorLayout(view: View) {
        NewsErrorLayout.visibility = View.GONE
        this.finish()
    }

    fun goFirstPage(view: View) {
        loadMaxPage()
    }

    fun goNextPage(view: View) {
        page = page + 1
        loadNews()
    }

    fun goPreviousPage(view: View) {
        page = page - 1
        loadNews()
    }
}
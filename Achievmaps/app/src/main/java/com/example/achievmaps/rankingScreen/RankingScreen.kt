package com.example.achievmaps.rankingScreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.achievmaps.R
import com.example.achievmaps.databaseConnections.DatabaseConnections
import com.example.achievmaps.loginScreen.LoginScreen
import kotlinx.android.synthetic.main.ranking_screen.*

@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER", "VARIABLE_WITH_REDUNDANT_INITIALIZER")
class RankingScreen : AppCompatActivity() {
    private var list = listOf("0")
    private var row = ArrayList<String>()
    private var record = ArrayList<ArrayList<String>>()
    private var table = ArrayList<ArrayList<ArrayList<String>>>()
    private var button1: Button? = null
    private var id = 1
    private var type = "All"
    private var page = -1
    private var userpage = -1
    private var maxpage = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.ranking_screen)
        button1 = RankingAllButton
        id = LoginScreen.loggedUserID
        type = "All"

        RankingView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        getRanking(RankingAllButton)
    }

    private fun setRankingEnabled(setting: Boolean) {
        RankingAllButton.isEnabled = setting
        RankingNatureButton.isEnabled = setting
        RankingArchitectureButton.isEnabled = setting
        RankingFirstPageButton.isEnabled = setting
        RankingNextPageButton.isEnabled = setting
        RankingDefaultPageButton.isEnabled = setting
        RankingPreviousPageButton.isEnabled = setting
    }

    fun closeRankingErrorLayout(view: View) {
        RankingErrorLayout.visibility = View.GONE
        setRankingEnabled(true)
        this.finish()
    }

    private fun getRanking(b1: Button?) {
        RankingLoadingScreen.visibility = View.VISIBLE
        setRankingEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            var rankingData = "-3"
            val t = Thread {
                rankingData =
                    DatabaseConnections.getTables(getString(R.string.url_text) + "getRanking.php?type=" + type)
                list = rankingData.split('\n')
            }
            t.start()
            t.join()

            if (list[0] == "-3") {
                RankingErrorText.text = getString(R.string.database_conn_error3_text)
                RankingErrorLayout.visibility = View.VISIBLE
                RankingLoadingScreen.visibility = View.GONE
            } else if (list[0] == "-2") {
                RankingErrorText.text = getString(R.string.database_conn_error2_text)
                RankingErrorLayout.visibility = View.VISIBLE
                RankingLoadingScreen.visibility = View.GONE
            } else {
                rankingPrepare()
                RankingLoadingScreen.visibility = View.GONE
                setRankingEnabled(true)
                if (b1 != button1) {
                    button1?.setBackgroundColor(getColor(R.color.button_green))
                    button1 = b1
                    button1?.setBackgroundColor(getColor(R.color.button_grayishgreen))
                }
                if (page == 0) {
                    RankingFirstPageButton.isEnabled = false
                    RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                    RankingNextPageButton.isEnabled = false
                    RankingNextPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                } else {
                    RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_green))
                    RankingNextPageButton.setBackgroundColor(getColor(R.color.button_green))
                }
                if (page == maxpage) {
                    RankingPreviousPageButton.isEnabled = false
                    RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                } else {
                    RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_green))
                }
                RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                RankingDefaultPageButton.isEnabled = false
                button1?.isEnabled = false
            }
        }, 100)
    }

    private fun rankingPrepare() {
        val t = Thread {
            table.clear()
            val newArr = ArrayList<String>()
            var prevScore = -1
            var rank = 0
            row.clear()
            record.clear()
            var poz = 0
            var rec = 0
            maxpage = 0
            for (item in list) {
                if (item == LoginScreen.loggedUserNick) {
                    page = maxpage
                    userpage = page
                }
                row.add(item)
                poz++
                if (poz >= 2) {
                    if (prevScore != row[1].toInt()) {
                        rank++
                        prevScore = row[1].toInt()
                    }
                    newArr.add(rank.toString())
                    newArr.add(row[0])
                    newArr.add(row[1])
                    poz = 0
                    record.add(newArr.clone() as ArrayList<String>)
                    row.clear()
                    newArr.clear()
                    rec++
                    if (rec >= 10) {
                        rec = 0
                        maxpage++
                        table.add(record.clone() as ArrayList<ArrayList<String>>)
                        record.clear()
                    }
                }
            }
            if (rec != 0) {
                table.add(record.clone() as ArrayList<ArrayList<String>>)
                record.clear()
            }
        }
        t.start()
        t.join()

        RankingView.swapAdapter(RankingAdapter(table[page]), true)
        RankingView.layoutManager = LinearLayoutManager(this)
    }

    fun goRankingAll(view: View) {
        type = "All"
        getRanking(RankingAllButton)
    }

    fun goRankingNature(view: View) {
        type = "Nature"
        getRanking(RankingNatureButton)
    }

    fun goRankingArchitecture(view: View) {
        type = "Architecture"
        getRanking(RankingArchitectureButton)
    }

    fun goFirstPage(view: View) {
        setRankingEnabled(false)
        page = 0
        Handler(Looper.getMainLooper()).postDelayed({
            RankingView.swapAdapter(RankingAdapter(table[page]), true)
            RankingView.layoutManager = LinearLayoutManager(this)
            setRankingEnabled(true)
            button1?.isEnabled = false
            if (page == 0) {
                RankingFirstPageButton.isEnabled = false
                RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                RankingNextPageButton.isEnabled = false
                RankingNextPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
            } else {
                RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_green))
                RankingNextPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
            if (page == maxpage) {
                RankingPreviousPageButton.isEnabled = false
                RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
            } else {
                RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
            if (page == userpage) {
                RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                RankingDefaultPageButton.isEnabled = false
            } else {
                RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
        }, 100)
    }

    fun goNextPage(view: View) {
        setRankingEnabled(false)
        page--
        Handler(Looper.getMainLooper()).postDelayed({
            RankingView.swapAdapter(RankingAdapter(table[page]), true)
            RankingView.layoutManager = LinearLayoutManager(this)
            setRankingEnabled(true)
            button1?.isEnabled = false
            if (page == 0) {
                RankingFirstPageButton.isEnabled = false
                RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                RankingNextPageButton.isEnabled = false
                RankingNextPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
            } else {
                RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_green))
                RankingNextPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
            if (page == maxpage) {
                RankingPreviousPageButton.isEnabled = false
                RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
            } else {
                RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
            if (page == userpage) {
                RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                RankingDefaultPageButton.isEnabled = false
            } else {
                RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
        }, 100)
    }

    fun goDefaultPage(view: View) {
        setRankingEnabled(false)
        page = userpage
        Handler(Looper.getMainLooper()).postDelayed({
            RankingView.swapAdapter(RankingAdapter(table[page]), true)
            RankingView.layoutManager = LinearLayoutManager(this)
            setRankingEnabled(true)
            button1?.isEnabled = false
            if (page == 0) {
                RankingFirstPageButton.isEnabled = false
                RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                RankingNextPageButton.isEnabled = false
                RankingNextPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
            } else {
                RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_green))
                RankingNextPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
            if (page == maxpage) {
                RankingPreviousPageButton.isEnabled = false
                RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
            } else {
                RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
            if (page == userpage) {
                RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                RankingDefaultPageButton.isEnabled = false
            } else {
                RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
        }, 100)
    }

    fun goPreviousPage(view: View) {
        setRankingEnabled(false)
        page++
        Handler(Looper.getMainLooper()).postDelayed({
            RankingView.swapAdapter(RankingAdapter(table[page]), true)
            RankingView.layoutManager = LinearLayoutManager(this)
            setRankingEnabled(true)
            button1?.isEnabled = false
            if (page == 0) {
                RankingFirstPageButton.isEnabled = false
                RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                RankingNextPageButton.isEnabled = false
                RankingNextPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
            } else {
                RankingFirstPageButton.setBackgroundColor(getColor(R.color.button_green))
                RankingNextPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
            if (page == maxpage) {
                RankingPreviousPageButton.isEnabled = false
                RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
            } else {
                RankingPreviousPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
            if (page == userpage) {
                RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                RankingDefaultPageButton.isEnabled = false
            } else {
                RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_green))
            }
        }, 100)
    }
}
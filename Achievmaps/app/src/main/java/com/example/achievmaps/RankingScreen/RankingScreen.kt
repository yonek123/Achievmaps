package com.example.achievmaps.rankingScreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.achievmaps.loginScreen.LoginScreen
import com.example.achievmaps.R
import kotlinx.android.synthetic.main.ranking_screen.*
import android.widget.Button
import com.example.achievmaps.databaseConnections.DatabaseConnections
import java.lang.Exception

class RankingScreen : AppCompatActivity() {
    private var list = listOf("0")
    private var row = ArrayList<String>()
    private var table = ArrayList<ArrayList<String>>()
    private var button1: Button? = null
    private var button2: Button? = null
    private var id = 1
    private var type = "All"
    private var page = -1
    private var maxpage = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.ranking_screen)
        button1 = RankingAllButton
        button2 = RankingDefaultPageButton
        id = LoginScreen.loggedUserID
        type = "All"

        RankingView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        RankingLoadingScreen.visibility = View.VISIBLE
        setRankingEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            val t = Thread {
                try {
                    page = DatabaseConnections.getTables(
                        "https://justsomephp.000webhostapp.com/getRankingUserPage.php?personid="
                                + id.toString() + "&type=" + type
                    ).toInt()
                    maxpage =
                        DatabaseConnections.getTables("https://justsomephp.000webhostapp.com/getRankingMaxPage.php")
                            .toInt()
                } catch (e: Exception) {
                    RankingErrorText.text = getString(R.string.database_conn_error3_text)
                    RankingErrorLayout.visibility = View.VISIBLE
                    RankingLoadingScreen.visibility = View.GONE
                }
            }
            t.start()
            t.join()
        }, 100)
        changeRanking(RankingAllButton, 1)
    }

    fun setRankingEnabled(setting: Boolean) {
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

    fun rankingPrepare() {
        row.clear()
        table.clear()
        var poz = 0
        for (item in list) {
            row.add(item)
            poz++
            if (poz > 3) {
                poz = 0
                table.add(row.clone() as ArrayList<String>)
                row.clear()
            }
        }

        RankingView.swapAdapter(RankingAdapter(table), true)
        RankingView.layoutManager = LinearLayoutManager(this)
    }

    fun changeRanking(b1: Button?, method: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            var rankingData = "-3"
            val t = Thread {
                try {
                    if (page == -1)
                        page = DatabaseConnections.getTables(
                            "https://justsomephp.000webhostapp.com/getRankingUserPage.php?personid="
                                    + id.toString() + "&type="
                                    + type
                        ).toInt()
                    if (maxpage <= page)
                        maxpage =
                            DatabaseConnections.getTables("https://justsomephp.000webhostapp.com/getRankingMaxPage.php")
                                .toInt()

                    if (method == 1) {
                        rankingData =
                            DatabaseConnections.getTables(
                                "https://justsomephp.000webhostapp.com/getRankingByUser.php?personid="
                                        + id.toString() + "&type="
                                        + type
                            )
                    } else {
                        rankingData =
                            DatabaseConnections.getTables(
                                "https://justsomephp.000webhostapp.com/getRankingByPage.php?page="
                                        + page.toString() + "&type="
                                        + type
                            )

                    }
                    list = rankingData.split('\n')

                } catch (e: Exception) {
                    rankingData = "-3"
                }
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
                if (button2 == RankingDefaultPageButton) {
                    RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_grayishgreen))
                    RankingDefaultPageButton.isEnabled = false
                } else {
                    RankingDefaultPageButton.setBackgroundColor(getColor(R.color.button_green))
                }
                button1?.isEnabled = false
            }
        }, 100)
    }

    fun goRankingAll(view: View) {
        RankingLoadingScreen.visibility = View.VISIBLE
        setRankingEnabled(false)
        type = "All"
        button2 = RankingDefaultPageButton
        changeRanking(RankingAllButton, 1)
    }

    fun goRankingNature(view: View) {
        RankingLoadingScreen.visibility = View.VISIBLE
        setRankingEnabled(false)
        type = "Nature"
        button2 = RankingDefaultPageButton
        changeRanking(RankingNatureButton, 1)
    }

    fun goRankingArchitecture(view: View) {
        RankingLoadingScreen.visibility = View.VISIBLE
        setRankingEnabled(false)
        type = "Architecture"
        button2 = RankingDefaultPageButton
        changeRanking(RankingArchitectureButton, 1)
    }

    fun goFirstPage(view: View) {
        RankingLoadingScreen.visibility = View.VISIBLE
        setRankingEnabled(false)
        page = 0
        button2 = RankingFirstPageButton
        changeRanking(button1, 2)
    }

    fun goNextPage(view: View) {
        RankingLoadingScreen.visibility = View.VISIBLE
        setRankingEnabled(false)
        page = page - 1
        button2 = RankingNextPageButton
        changeRanking(button1, 2)
    }

    fun goDefaultPage(view: View) {
        RankingLoadingScreen.visibility = View.VISIBLE
        setRankingEnabled(false)
        page = -1
        button2 = RankingDefaultPageButton
        changeRanking(button1, 2)
    }

    fun goPreviousPage(view: View) {
        RankingLoadingScreen.visibility = View.VISIBLE
        setRankingEnabled(false)
        page = page + 1
        button2 = RankingPreviousPageButton
        changeRanking(button1, 2)
    }
}
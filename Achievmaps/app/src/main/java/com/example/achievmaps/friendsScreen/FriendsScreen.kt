package com.example.achievmaps.friendsScreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.achievmaps.databaseConnections.Friends
import com.example.achievmaps.loginScreen.LoginScreen
import com.example.achievmaps.R
import kotlinx.android.synthetic.main.friends_screen.*
import android.widget.Button
import com.example.achievmaps.friendsScreen.FriendsAdapter
import java.lang.Exception

class FriendsScreen : AppCompatActivity() {
    private var list = listOf("0")
    private var row = ArrayList<String>()
    private var table = ArrayList<ArrayList<String>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.friends_screen)

        FriendsView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        FriendsLoadingScreen.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            var friendsData = "-3"
            val t = Thread {
                friendsData =
                    Friends.getFriends(LoginScreen.loggedUserID)
                list = friendsData.split('\n')
            }
            t.start()
            t.join()

            if (list[0] == "-3") {
                FriendsErrorText.text = getString(R.string.database_conn_error3_text)
                FriendsErrorLayout.visibility = View.VISIBLE
                FriendsLoadingScreen.visibility = View.GONE
            } else if (list[0] == "-2") {
                FriendsErrorText.text = getString(R.string.database_conn_error2_text)
                FriendsErrorLayout.visibility = View.VISIBLE
                FriendsLoadingScreen.visibility = View.GONE
            } else {
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

                FriendsView.swapAdapter(FriendsAdapter(table), true)
                FriendsView.layoutManager = LinearLayoutManager(this)
                FriendsLoadingScreen.visibility = View.GONE
            }
        }, 100)
    }

    fun closeFriendsErrorLayout(view: View) {
        FriendsErrorLayout.visibility = View.GONE
        this.finish()
    }
}
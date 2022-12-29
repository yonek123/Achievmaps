package com.example.achievmaps.friendsScreen

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.achievmaps.loginScreen.LoginScreen
import com.example.achievmaps.R
import com.example.achievmaps.databaseConnections.DatabaseConnections
import kotlinx.android.synthetic.main.friends_screen.*

@Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER", "UNCHECKED_CAST", "UNUSED_PARAMETER")
class FriendsScreen : AppCompatActivity() {
    private var list = listOf("0")
    private var row = ArrayList<String>()
    private var table = ArrayList<ArrayList<String>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.friends_screen)
        loadFriends()
    }

    private fun loadFriends() {
        FriendsView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        FriendsLoadingScreen.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            var friendsData = "-3"
            val t = Thread {
                friendsData = DatabaseConnections.getTables(
                    getString(R.string.url_text) + "getFriends.php?personid="
                            + LoginScreen.loggedUserID
                )
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

    fun openAddFriendLayout(view: View) {
        FriendsAddDeleteText.text = getString(R.string.add_friend_text)
        FriendsAddDeleteButton.text = getString(R.string.add_text)
        FriendsAddDeleteButton.setOnClickListener {
            addFriend(it)
        }
        FriendsAddDeleteLayout.visibility = View.VISIBLE
        AddFriendButton.isEnabled = false
        DeleteFriendButton.isEnabled = false
    }

    fun openDeleteFriendLayout(view: View) {
        FriendsAddDeleteText.text = getString(R.string.delete_friend_text)
        FriendsAddDeleteButton.text = getString(R.string.delete_text)
        FriendsAddDeleteButton.setOnClickListener {
            deleteFriend(it)
        }
        FriendsAddDeleteLayout.visibility = View.VISIBLE
        AddFriendButton.isEnabled = false
        DeleteFriendButton.isEnabled = false
    }

    fun closeAddFriendLayout(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        FriendsChoiceBox.visibility = View.VISIBLE
        AddDeleteFriendCloseBox.visibility = View.GONE
        FriendsAddDeleteLayout.visibility = View.GONE
        FriendNickValidationText.visibility = View.GONE
        AddFriendButton.isEnabled = true
        DeleteFriendButton.isEnabled = true
    }

    fun addFriend(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        FriendsLoadingScreen.visibility = View.VISIBLE
        FriendsAddDeleteButton.isEnabled = false
        FriendsAddDeleteCloseButton.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            var isSuccess = "-3"
            var lines = listOf("0")
            val t = Thread {
                isSuccess = DatabaseConnections.getTables(
                    getString(R.string.url_text) + "addFriend.php?personid="
                            + LoginScreen.loggedUserID + "&friendnick="
                            + AddFriendNickBox.text.toString()
                )
                lines = isSuccess.split('\n')
            }
            t.start()
            t.join()

            if (lines[0] == "-3") {
                FriendNickValidationText.text = getString(R.string.database_conn_error3_text)
                FriendNickValidationText.visibility = View.VISIBLE
            } else if (lines[0] == "-2") {
                FriendNickValidationText.text = getString(R.string.database_conn_error2_text)
                FriendNickValidationText.visibility = View.VISIBLE
            } else if (lines[0] == "-1") {
                FriendNickValidationText.text = getString(R.string.already_friends_text)
                FriendNickValidationText.visibility = View.VISIBLE
            } else if (lines[0] == "-4") {
                FriendNickValidationText.text = getString(R.string.wrong_user_text)
                FriendNickValidationText.visibility = View.VISIBLE
            } else {
                AddDeleteFriendCloseText.text = getString(R.string.friend_added_text)
                FriendsChoiceBox.visibility = View.GONE
                AddDeleteFriendCloseBox.visibility = View.VISIBLE
                loadFriends()
            }
            FriendsLoadingScreen.visibility = View.GONE
            FriendsAddDeleteButton.isEnabled = true
            FriendsAddDeleteCloseButton.isEnabled = true
        }, 100)
    }

    private fun deleteFriend(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        FriendsLoadingScreen.visibility = View.VISIBLE
        FriendsAddDeleteButton.isEnabled = false
        FriendsAddDeleteCloseButton.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            var isSuccess = "-3"
            var lines = listOf("0")
            val t = Thread {
                isSuccess = DatabaseConnections.getTables(
                    getString(R.string.url_text) + "deleteFriend.php?personid="
                            + LoginScreen.loggedUserID + "&friendnick="
                            + AddFriendNickBox.text.toString()
                )
                lines = isSuccess.split('\n')
            }
            t.start()
            t.join()

            if (lines[0] == "-3") {
                FriendNickValidationText.text = getString(R.string.database_conn_error3_text)
                FriendNickValidationText.visibility = View.VISIBLE
            } else if (lines[0] == "-2") {
                FriendNickValidationText.text = getString(R.string.database_conn_error2_text)
                FriendNickValidationText.visibility = View.VISIBLE
            } else if (lines[0] == "-1") {
                FriendNickValidationText.text = getString(R.string.not_friends_text)
                FriendNickValidationText.visibility = View.VISIBLE
            } else if (lines[0] == "-4") {
                FriendNickValidationText.text = getString(R.string.wrong_user_text)
                FriendNickValidationText.visibility = View.VISIBLE
            } else {
                AddDeleteFriendCloseText.text = getString(R.string.friend_deleted_text)
                FriendsChoiceBox.visibility = View.GONE
                AddDeleteFriendCloseBox.visibility = View.VISIBLE
                loadFriends()
            }
            FriendsLoadingScreen.visibility = View.GONE
            FriendsAddDeleteButton.isEnabled = true
            FriendsAddDeleteCloseButton.isEnabled = true
        }, 100)
    }
}
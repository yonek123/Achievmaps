package com.example.achievmaps.loginScreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.achievmaps.R
import com.example.achievmaps.databaseConnections.DatabaseConnections
import com.example.achievmaps.mainMenuScreen.MainMenuScreen
import kotlinx.android.synthetic.main.login_screen.*


@Suppress("UNUSED_PARAMETER", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "CascadeIf")
class LoginScreen : AppCompatActivity() {
    private val sharedPrefFile = "AchievmapsPref"

    companion object {
        var loggedUserID = -3
        var loggedUserEmail = "email@email.com"
        var loggedUserNick = "nick"
        var loggedUserPassword = "password"
        var loggedUserPointsAll = 0
        var loggedUserPointsNature = 0
        var loggedUserPointsArchitecture = 0
    }

    override fun onResume() {
        super.onResume()
        LoginLoadingScreen.visibility = View.GONE
        setLoginRegisterEnabled(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val sharedPref = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean("isSaved", false)) {
            LoginEmailField.setText(
                sharedPref.getString(
                    "email",
                    getString(R.string.email_field_text)
                )
            )
            LoginPasswordField.setText(
                sharedPref.getString(
                    "password",
                    getString(R.string.password_field_text)
                )
            )
            LoginRememberMe.isChecked = true
        }
    }

    private fun setLoginRegisterEnabled(setting: Boolean) {
        LoginEmailField.isEnabled = setting
        LoginPasswordField.isEnabled = setting
        LoginLoginButton.isEnabled = setting
        LoginRegisterButton.isEnabled = setting
        LoginPasswordRecoveryButton.isEnabled = setting
        LoginRememberMe.isEnabled = setting
        RegisterEmailField.isEnabled = setting
        RegisterNickField.isEnabled = setting
        RegisterPasswordField.isEnabled = setting
        RegisterRepeatPasswordField.isEnabled = setting
        RegisterRegisterButton.isEnabled = setting
        RegisterBackButton.isEnabled = setting
    }

    fun recoverPasswordLayout(view: View) {
        PasswordRecoveryP1.visibility = View.VISIBLE
        RecoverySuccessText.visibility = View.GONE
        PasswordRecoveryLayout.visibility = View.VISIBLE
        setLoginRegisterEnabled(false)
    }

    @SuppressLint("SetTextI18n")
    fun sendRecoveryCode(view: View) {
        LoginLoadingScreen.visibility = View.VISIBLE
        RecoveryCloseButton.isEnabled = false
        PasswordRecoveryButton.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            var password = "-3"
            val t = Thread {
                password = DatabaseConnections.getTables(
                    getString(R.string.url_text) + "passwordRecovery.php?email="
                            + RecoveryEmailField.text.toString()
                )
                password = password.split('\n')[0]
            }
            t.start()
            t.join()

            if (password == "-3") {
                RecoverySuccessText.text = getString(R.string.database_conn_error3_text)
            } else if (password == "-2") {
                RecoverySuccessText.text = getString(R.string.database_conn_error3_text)
            } else if (password == "-1") {
                RecoverySuccessText.text = getString(R.string.wrong_email_text)
            } else {
                RecoverySuccessText.text =
                    getString(R.string.recovery_code_sent_text) + '\n' + password
            }
            RecoverySuccessText.visibility = View.VISIBLE
            LoginLoadingScreen.visibility = View.GONE
            PasswordRecoveryP1.visibility = View.GONE
            RecoveryCloseButton.isEnabled = true
            PasswordRecoveryButton.isEnabled = true
        }, 100)
    }

    fun closeRecoveryLayout(view: View) {
        PasswordRecoveryLayout.visibility = View.GONE
        setLoginRegisterEnabled(true)
    }

    fun loginCompleted(view: View) {
        LoginLoadingScreen.visibility = View.VISIBLE
        setLoginRegisterEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPref = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            if (LoginRememberMe.isChecked) {
                editor.putString("email", LoginEmailField.text.toString())
                editor.putString("password", LoginPasswordField.text.toString())
                editor.putBoolean("isSaved", true)
                editor.apply()
            } else {
                editor.putString("email", "")
                editor.putString("password", "")
                editor.putBoolean("isSaved", false)
                editor.apply()
            }

            var loggedUserData = "-3"
            var lines = listOf("0")
            val t = Thread {
                loggedUserData = DatabaseConnections.getTables(
                    getString(R.string.url_text) + "login.php?email="
                            + LoginEmailField.text.toString() + "&password="
                            + LoginPasswordField.text.toString()
                )
                lines = loggedUserData.split('\n')
                loggedUserID = lines[0].toInt()
            }
            t.start()
            t.join()

            if (loggedUserID == -3) {
                LoginValidationText.text = getString(R.string.database_conn_error3_text)
                LoginValidationText.visibility = View.VISIBLE
                LoginLoadingScreen.visibility = View.GONE
                setLoginRegisterEnabled(true)
            } else if (loggedUserID == -2) {
                LoginValidationText.text = getString(R.string.database_conn_error2_text)
                LoginValidationText.visibility = View.VISIBLE
                LoginLoadingScreen.visibility = View.GONE
                setLoginRegisterEnabled(true)
            } else if (loggedUserID == -1) {
                LoginValidationText.text = getString(R.string.login_wrong_email_or_password_text)
                LoginValidationText.visibility = View.VISIBLE
                LoginLoadingScreen.visibility = View.GONE
                setLoginRegisterEnabled(true)
            } else {
                loggedUserEmail = lines[1]
                loggedUserPassword = lines[2]
                loggedUserNick = lines[3]
                loggedUserPointsAll = lines[4].toInt()
                loggedUserPointsNature = lines[5].toInt()
                loggedUserPointsArchitecture = lines[6].toInt()
                LoginValidationText.visibility = View.GONE
                val intent = Intent(this, MainMenuScreen::class.java)
                startActivity(intent)
            }
        }, 100)
    }

    fun registerValidate(view: View) {
        LoginLoadingScreen.visibility = View.VISIBLE
        setLoginRegisterEnabled(false)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(RegisterEmailField.text.toString())
                    .matches()
            ) {
                RegisterValidationText.text = getString(R.string.wrong_email_text)
                RegisterValidationText.visibility = View.VISIBLE
                setLoginRegisterEnabled(true)
            } else if (RegisterNickField.text.toString().length < 5) {
                RegisterValidationText.text = getString(R.string.nick_too_short_text)
                RegisterValidationText.visibility = View.VISIBLE
                setLoginRegisterEnabled(true)
            } else if (RegisterPasswordField.text.toString().length < 8) {
                RegisterValidationText.text = getString(R.string.password_too_short_text)
                RegisterValidationText.visibility = View.VISIBLE
                setLoginRegisterEnabled(true)
            } else if (RegisterPasswordField.text.toString() != RegisterRepeatPasswordField.text.toString()) {
                RegisterValidationText.text = getString(R.string.passwords_not_equal_text)
                RegisterValidationText.visibility = View.VISIBLE
                setLoginRegisterEnabled(true)
            } else {
                var registerSuccess: Int = -3
                val t = Thread {
                    LoginLoadingScreen.visibility = View.VISIBLE
                    registerSuccess =
                        DatabaseConnections.getTables(
                            getString(R.string.url_text) + "register.php?email="
                                    + RegisterEmailField.text.toString() + "&nickname="
                                    + RegisterNickField.text.toString() + "&password="
                                    + RegisterPasswordField.text.toString()
                        ).toInt()
                }
                t.start()
                t.join()
                LoginLoadingScreen.visibility = View.GONE

                if (registerSuccess == -3) {
                    RegisterValidationText.text = getString(R.string.database_conn_error3_text)
                    RegisterValidationText.visibility = View.VISIBLE
                    setLoginRegisterEnabled(true)
                } else if (registerSuccess == -2) {
                    RegisterValidationText.text = getString(R.string.database_conn_error2_text)
                    RegisterValidationText.visibility = View.VISIBLE
                    setLoginRegisterEnabled(true)
                } else if (registerSuccess == 1) {
                    RegisterValidationText.text =
                        getString(R.string.register_email_already_exist_text)
                    RegisterValidationText.visibility = View.VISIBLE
                    setLoginRegisterEnabled(true)
                } else if (registerSuccess == 2) {
                    RegisterValidationText.text =
                        getString(R.string.register_nick_already_exist_text)
                    RegisterValidationText.visibility = View.VISIBLE
                    setLoginRegisterEnabled(true)
                } else {
                    RegisterSuccessLayout.visibility = View.VISIBLE
                }
            }
            LoginLoadingScreen.visibility = View.GONE
        }, 100)
    }

    fun closeRegisterSuccessLayout(view: View) {
        RegisterSuccessLayout.visibility = View.GONE
        RegisterValidationText.visibility = View.GONE
        visibleLogin(view)
        setLoginRegisterEnabled(true)
    }

    fun visibleRegister(view: View) {
        LoginLayout.visibility = View.GONE
        RegisterLayout.visibility = View.VISIBLE
        RegisterValidationText.visibility = View.GONE
    }

    fun visibleLogin(view: View) {
        LoginLayout.visibility = View.VISIBLE
        LoginValidationText.visibility = View.GONE
        RegisterLayout.visibility = View.GONE
    }
}
package com.example.achievmaps.loginScreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.example.achievmaps.R
import com.example.achievmaps.databaseConnections.Login
import com.example.achievmaps.databaseConnections.Register
import com.example.achievmaps.mainMenuScreen.MainMenuScreen
import kotlinx.android.synthetic.main.login_screen.*


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

    fun setLoginRegisterEnabled(setting: Boolean) {
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
        PasswordRecoveryLayout.visibility = View.VISIBLE
        PasswordRecoveryP1.visibility = View.VISIBLE
        RecoverySuccessText.visibility = View.GONE
        setLoginRegisterEnabled(false)
    }

    fun sendRecoveryCode(view: View) {
        PasswordRecoveryP1.visibility = View.GONE
        RecoverySuccessText.visibility = View.VISIBLE
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
                loggedUserData =
                    Login.login(LoginEmailField.text.toString(), LoginPasswordField.text.toString())
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
                val intent = Intent(this, MainMenuScreen::class.java)
                startActivity(intent)
            }
        }, 1000)
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
            } else if (RegisterNickField.text.toString().length < 5) {
                RegisterValidationText.text = getString(R.string.nick_too_short_text)
                RegisterValidationText.visibility = View.VISIBLE
            } else if (RegisterPasswordField.text.toString().length < 8) {
                RegisterValidationText.text = getString(R.string.password_too_short_text)
                RegisterValidationText.visibility = View.VISIBLE
            } else if (RegisterPasswordField.text.toString() != RegisterRepeatPasswordField.text.toString()) {
                RegisterValidationText.text = getString(R.string.passwords_not_equal_text)
                RegisterValidationText.visibility = View.VISIBLE
            } else {
                var registerSuccess: Int = -3
                val t = Thread {
                    LoginLoadingScreen.visibility = View.VISIBLE
                    registerSuccess =
                        Register.register(
                            RegisterEmailField.text.toString(),
                            RegisterNickField.text.toString(),
                            RegisterPasswordField.text.toString()
                        )
                }
                t.start()
                t.join()
                LoginLoadingScreen.visibility = View.GONE

                if (registerSuccess == -3) {
                    RegisterValidationText.text = getString(R.string.database_conn_error3_text)
                    RegisterValidationText.visibility = View.VISIBLE
                } else if (registerSuccess == -2) {
                    RegisterValidationText.text = getString(R.string.database_conn_error2_text)
                    RegisterValidationText.visibility = View.VISIBLE
                } else if (registerSuccess == 1) {
                    RegisterValidationText.text =
                        getString(R.string.register_email_already_exist_text)
                    RegisterValidationText.visibility = View.VISIBLE
                } else if (registerSuccess == 2) {
                    RegisterValidationText.text =
                        getString(R.string.register_nick_already_exist_text)
                    RegisterValidationText.visibility = View.VISIBLE
                } else {
                    RegisterSuccessLayout.visibility = View.VISIBLE
                    setLoginRegisterEnabled(false)
                }
            }
        }, 1000)
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
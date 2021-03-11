package org.openvoipalliance.voiplibexample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.config.Auth
import org.openvoipalliance.voiplib.config.Config
import org.openvoipalliance.voiplib.model.RegistrationState.*
import org.openvoipalliance.voiplib.repository.initialise.CallListener

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        login.setOnClickListener {
            val account = sip_account.text.toString()
            val password = sip_password.text.toString()
            val serverIP = sip_server.text.toString()
            val port = sip_port.text.toString()
            val encrypted = sip_encrypted.isChecked
            val voipLib = VoIPLib.getInstance(this)

            val config = Config(
                    auth = Auth(account, password, serverIP, port.toInt()),
                    encryption = encrypted,
                    callListener = object : CallListener {}
            )

            voipLib.apply {
                initialise(config)
                unregister()
                register {
                    when (it) {
                        REGISTERED -> {
                            Toast.makeText(this@LoginActivity, getString(R.string.successfully_logged_in), Toast.LENGTH_SHORT).show()
                            goToMainActivity()
                        }
                        CLEARED -> Toast.makeText(this@LoginActivity, getString(R.string.successfully_unregistered), Toast.LENGTH_SHORT).show()
                        FAILED -> {
                            Log.e(TAG, "registrationFailed: ")
                            Toast.makeText(this@LoginActivity, getString(R.string.registration_failed), Toast.LENGTH_SHORT).show()
                        }
                        else -> {

                        }
                    }
                }
            }
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
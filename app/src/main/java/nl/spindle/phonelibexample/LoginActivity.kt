package nl.spindle.phonelibexample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import nl.spindle.phonelib.PhoneLib
import nl.spindle.phonelib.model.RegistrationState
import nl.spindle.phonelib.repository.registration.RegistrationCallback
import nl.spindle.phonelib.service.LinphoneService

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        handleCurrentState()
        setOnClickListeners()
    }

    private fun handleCurrentState() {
        if (LinphoneService.isInitialised) {
            goToMainActivity()
        } else {
            PhoneLib.getInstance(this).initialise()
        }
    }

    private fun setOnClickListeners() {
        login.setOnClickListener {
            val account = sip_account.text.toString()
            val password = sip_password.text.toString()
            val serverIP = sip_server.text.toString()
            val port = sip_port.text.toString()
            val encrypted = sip_encrypted.isChecked
            PhoneLib.getInstance(this).unregister()
            PhoneLib.getInstance(this).resetAudioCodecs()
            PhoneLib.getInstance(this).register(account, password, serverIP, port, null, encrypted, object : RegistrationCallback() {
                override fun stateChanged(registrationState: RegistrationState) {
                    super.stateChanged(registrationState)
                    when (registrationState) {
                        RegistrationState.REGISTERED -> {
                            Toast.makeText(this@LoginActivity, getString(R.string.successfully_logged_in), Toast.LENGTH_SHORT).show()
                            goToMainActivity()
                        }
                        RegistrationState.CLEARED -> Toast.makeText(this@LoginActivity, getString(R.string.successfully_unregistered), Toast.LENGTH_SHORT).show()
                        RegistrationState.NONE -> Log.d(TAG, "registrationNONE: ")
                        RegistrationState.UNKNOWN -> Log.d(TAG, "registrationUnknown: ")
                        else -> {
                            Log.e(TAG, "registrationFailed: ")
                            Toast.makeText(this@LoginActivity, getString(R.string.registration_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
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
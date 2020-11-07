package org.openvoipalliance.phonelibexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.openvoipalliance.phonelib.PhoneLib
import org.openvoipalliance.phonelib.model.AttendedTransferSession
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.repository.initialise.CallListener
import java.util.concurrent.TimeUnit

private const val REQUEST_MICROPHONE_PERMISSION = 2

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private var activeCall: Call? = null
    private var secondCall: Call? = null
    private var attendedTransferSession: AttendedTransferSession? = null

    private val phoneLib
        get() = PhoneLib.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        phoneLib.swapConfig(phoneLib.currentConfig.copy(callListener = listener))
        setClickListeners()
    }

    private fun setDuration() {
        handler = Handler()
        handler.postDelayed({
            duration.text = activeCall?.duration.toString()
            if (activeCall != null) {
                setDuration()
            }
        }, TimeUnit.SECONDS.toMillis(1))
    }

    @SuppressLint("MissingPermission")
    private fun setClickListeners() {
        audio_call.setOnClickListener {
            val dialNum = dial_number.text.toString()
            if (hasAudioCallPermissions()) {
                activeCall = phoneLib.callTo(dialNum)
            } else {
                Log.e(TAG, "No permission granted")
            }
        }

        unregister.setOnClickListener {
            PhoneLib.getInstance(this@MainActivity).unregister()
        }
        hang_up.setOnClickListener {
            activeCall?.let {
                phoneLib.actions(it).end()
            }
        }
        transfer.setOnClickListener {
            activeCall?.let {
                phoneLib.actions(it).transferUnattended(dial_number.text.toString())
            }
        }
        add_call.setOnClickListener {
            activeCall?.let {
                secondCall = phoneLib.callTo(dial_number.text.toString())
                secondCall?.let { newSession ->
                    phoneLib.actions(it).switchActiveCall(newSession)
                    activeCall = newSession
                    secondCall = it
                }
            }
        }
        switch_calls.setOnClickListener {
            activeCall?.let {
                secondCall?.let { newSession ->
                    phoneLib.actions(it).switchActiveCall(newSession)
                    activeCall = newSession
                    secondCall = it
                }
            }
        }
        attended_transfer.setOnClickListener {
            activeCall?.let {
                attendedTransferSession = phoneLib.actions(it).beginAttendedTransfer(dial_number.text.toString())
                merge_calls.visibility = View.VISIBLE
            }
        }
        merge_calls.setOnClickListener {
            attendedTransferSession?.let {
                phoneLib.actions(it.from).finishAttendedTransfer(it)
            }
        }
        accept_call.setOnClickListener {
            if (hasAudioCallPermissions()) {
                activeCall?.let {
                    phoneLib.actions(it).accept()
                }
            } else {
                Log.e(TAG, "No permission granted")
            }
        }
        toggle_mute.setOnCheckedChangeListener{_, checked ->
            phoneLib.microphoneMuted = !checked
        }
        toggle_hold.setOnCheckedChangeListener{_, checked ->
            activeCall?.let {
                phoneLib.actions(it).hold(checked)
            }
        }
        logout.setOnClickListener {
            PhoneLib.getInstance(this@MainActivity).destroy()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun hasAudioCallPermissions(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE_PERMISSION)
            false
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private val listener = object : CallListener {
        override fun incomingCallReceived(call: Call) {
            activeCall = call
            accept_call.visibility = View.VISIBLE
            hang_up.visibility = View.VISIBLE
            transfer.visibility = View.VISIBLE
            attended_transfer.visibility = View.VISIBLE
            add_call.visibility = View.VISIBLE
            switch_calls.visibility = View.VISIBLE
            caller.text = call.displayName
            setDuration()
        }

        override fun outgoingCallCreated(call: Call) {
            hang_up.visibility = View.VISIBLE
            transfer.visibility = View.VISIBLE
            attended_transfer.visibility = View.VISIBLE
            add_call.visibility = View.VISIBLE
            switch_calls.visibility = View.VISIBLE
        }

        override fun callConnected(call: Call) {
            phoneLib.microphoneMuted = false
            accept_call.visibility = View.GONE
            call_buttons.visibility = View.VISIBLE
            call_data.visibility = View.VISIBLE
            caller.text = call.displayName
            setDuration()
        }

        override fun callEnded(call: Call) {
            accept_call.visibility = View.GONE
            hang_up.visibility = View.GONE
            transfer.visibility = View.GONE
            attended_transfer.visibility = View.GONE
            add_call.visibility = View.GONE
            switch_calls.visibility = View.GONE
            merge_calls.visibility = View.GONE
            call_buttons.visibility = View.GONE
            call_data.visibility = View.GONE
            activeCall = null
        }
    }
}
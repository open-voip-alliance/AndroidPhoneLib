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
import org.openvoipalliance.phonelib.repository.initialise.SessionCallback
import java.util.concurrent.TimeUnit

private const val REQUEST_MICROPHONE_PERMISSION = 2
private const val REQUEST_VIDEO_PERMISSION = 3

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private var activeCall: Call? = null
    private var secondCall: Call? = null
    private var attendedTransferSession: AttendedTransferSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSipCallbacks()
        setClickListeners()
    }

    private fun setSipCallbacks() {
        PhoneLib.getInstance(this).setSessionCallback(object : SessionCallback() {
            override fun incomingCall(incomingCall: Call) {
                super.incomingCall(incomingCall)
                activeCall = incomingCall
                accept_call.visibility = View.VISIBLE
                hang_up.visibility = View.VISIBLE
                transfer.visibility = View.VISIBLE
                attended_transfer.visibility = View.VISIBLE
                add_call.visibility = View.VISIBLE
                switch_calls.visibility = View.VISIBLE
                caller.text = incomingCall.displayName
                setDuration()
            }

            override fun outgoingInit(call: Call) {
                super.outgoingInit(call)
                hang_up.visibility = View.VISIBLE
                transfer.visibility = View.VISIBLE
                attended_transfer.visibility = View.VISIBLE
                add_call.visibility = View.VISIBLE
                switch_calls.visibility = View.VISIBLE
            }

            override fun sessionConnected(call: Call) {
                super.sessionConnected(call)
                PhoneLib.getInstance(this@MainActivity).setMicrophone(true)
                accept_call.visibility = View.GONE
                call_buttons.visibility = View.VISIBLE
                call_data.visibility = View.VISIBLE
                caller.text = call.displayName
                setDuration()
            }

            override fun sessionReleased(call: Call) {
                super.sessionReleased(call)
                sendBroadcast(Intent(VideoActivity.RECEIVE_FINISH_VIDEO_ACTIVITY))
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

            override fun sessionEnded(call: Call) {
                super.sessionEnded(call)
                sendBroadcast(Intent(VideoActivity.RECEIVE_FINISH_VIDEO_ACTIVITY))
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
        })
    }

    private fun setDuration() {
        handler = Handler();
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
                PhoneLib.getInstance(this@MainActivity).callTo(dialNum, false)?.let {
                    activeCall = it
                }
            } else {
                Log.e(TAG, "No permission granted")
            }
        }
        video_call.setOnClickListener {
            val dialNum = dial_number.text.toString()
            if (hasVideoCallPermissions() && hasAudioCallPermissions()) {
                PhoneLib.getInstance(this@MainActivity).callTo(dialNum, true)?.let {
                    activeCall = it
                }
                startActivity(Intent(this@MainActivity, VideoActivity::class.java))
            } else {
                Log.e(TAG, "No permission granted")
            }
        }
        unregister.setOnClickListener {
            PhoneLib.getInstance(this@MainActivity).unregister()
        }
        hang_up.setOnClickListener {
            activeCall?.let {
                PhoneLib.getInstance(this@MainActivity).end(it)
            }
        }
        transfer.setOnClickListener {
            activeCall?.let {
                PhoneLib.getInstance(this@MainActivity).transferUnattended(it, dial_number.text.toString())
            }
        }
        add_call.setOnClickListener {
            activeCall?.let {
                secondCall = PhoneLib.getInstance(this@MainActivity).callTo(dial_number.text.toString())
                secondCall?.let { newSession ->
                    PhoneLib.getInstance(this@MainActivity).switchSession(it, newSession)
                    activeCall = newSession
                    secondCall = it
                }
            }
        }
        switch_calls.setOnClickListener {
            activeCall?.let {
                secondCall?.let { newSession ->
                    PhoneLib.getInstance(this@MainActivity).switchSession(it, newSession)
                    activeCall = newSession
                    secondCall = it
                }
            }
        }
        attended_transfer.setOnClickListener {
            activeCall?.let {
                attendedTransferSession = PhoneLib.getInstance(this@MainActivity).beginAttendedTransfer(it, dial_number.text.toString())
                merge_calls.visibility = View.VISIBLE
            }
        }
        merge_calls.setOnClickListener {
            attendedTransferSession?.let {
                PhoneLib.getInstance(this@MainActivity).finishAttendedTransfer(it)
            }
        }
        accept_call.setOnClickListener {
            if (hasAudioCallPermissions()) {
                activeCall?.let {
                    PhoneLib.getInstance(this@MainActivity).acceptIncoming(it)
                }
                if (PhoneLib.getInstance(this@MainActivity).isVideoEnabled()) {
                    startActivity(Intent(this@MainActivity, VideoActivity::class.java))
                }
            } else {
                Log.e(TAG, "No permission granted")
            }
        }
        toggle_mute.setOnCheckedChangeListener{_, checked ->
            PhoneLib.getInstance(this@MainActivity).setMicrophone(!checked)
        }
        toggle_hold.setOnCheckedChangeListener{_, checked ->
            activeCall?.let {
                PhoneLib.getInstance(this@MainActivity).setHold(it, checked)
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

    private fun hasVideoCallPermissions(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA), REQUEST_VIDEO_PERMISSION)
            false
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
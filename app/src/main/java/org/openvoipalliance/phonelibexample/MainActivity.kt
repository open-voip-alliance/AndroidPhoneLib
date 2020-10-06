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
import org.openvoipalliance.phonelib.model.Session
import org.openvoipalliance.phonelib.repository.initialise.SessionCallback
import java.util.concurrent.TimeUnit

private const val REQUEST_MICROPHONE_PERMISSION = 2
private const val REQUEST_VIDEO_PERMISSION = 3

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private var activeSession: Session? = null
    private var secondSession: Session? = null
    private var attendedTransferSession: AttendedTransferSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSipCallbacks()
        setClickListeners()
    }

    private fun setSipCallbacks() {
        PhoneLib.getInstance(this).setSessionCallback(object : SessionCallback() {
            override fun incomingCall(incomingSession: Session) {
                super.incomingCall(incomingSession)
                activeSession = incomingSession
                accept_call.visibility = View.VISIBLE
                hang_up.visibility = View.VISIBLE
                transfer.visibility = View.VISIBLE
                attended_transfer.visibility = View.VISIBLE
                add_call.visibility = View.VISIBLE
                switch_calls.visibility = View.VISIBLE
                caller.text = incomingSession.displayName
                setDuration()
            }

            override fun outgoingInit(session: Session) {
                super.outgoingInit(session)
                hang_up.visibility = View.VISIBLE
                transfer.visibility = View.VISIBLE
                attended_transfer.visibility = View.VISIBLE
                add_call.visibility = View.VISIBLE
                switch_calls.visibility = View.VISIBLE
            }

            override fun sessionConnected(session: Session) {
                super.sessionConnected(session)
                PhoneLib.getInstance(this@MainActivity).setMicrophone(true)
                accept_call.visibility = View.GONE
                call_buttons.visibility = View.VISIBLE
                call_data.visibility = View.VISIBLE
                caller.text = session.displayName
                setDuration()
            }

            override fun sessionReleased(session: Session) {
                super.sessionReleased(session)
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
                activeSession = null
            }

            override fun sessionEnded(session: Session) {
                super.sessionEnded(session)
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
                activeSession = null
            }
        })
    }

    private fun setDuration() {
        handler = Handler();
        handler.postDelayed({
            duration.text = activeSession?.duration.toString()
            if (activeSession != null) {
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
                    activeSession = it
                }
            } else {
                Log.e(TAG, "No permission granted")
            }
        }
        video_call.setOnClickListener {
            val dialNum = dial_number.text.toString()
            if (hasVideoCallPermissions() && hasAudioCallPermissions()) {
                PhoneLib.getInstance(this@MainActivity).callTo(dialNum, true)?.let {
                    activeSession = it
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
            activeSession?.let {
                PhoneLib.getInstance(this@MainActivity).end(it)
            }
        }
        transfer.setOnClickListener {
            activeSession?.let {
                PhoneLib.getInstance(this@MainActivity).transferUnattended(it, dial_number.text.toString())
            }
        }
        add_call.setOnClickListener {
            activeSession?.let {
                secondSession = PhoneLib.getInstance(this@MainActivity).callTo(dial_number.text.toString())
                secondSession?.let { newSession ->
                    PhoneLib.getInstance(this@MainActivity).switchSession(it, newSession)
                    activeSession = newSession
                    secondSession = it
                }
            }
        }
        switch_calls.setOnClickListener {
            activeSession?.let {
                secondSession?.let { newSession ->
                    PhoneLib.getInstance(this@MainActivity).switchSession(it, newSession)
                    activeSession = newSession
                    secondSession = it
                }
            }
        }
        attended_transfer.setOnClickListener {
            activeSession?.let {
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
                activeSession?.let {
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
            activeSession?.let {
                PhoneLib.getInstance(this@MainActivity).setHold(it, checked)
            }
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
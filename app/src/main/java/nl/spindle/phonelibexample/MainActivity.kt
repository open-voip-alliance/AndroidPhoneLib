package nl.spindle.phonelibexample

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
import nl.spindle.phonelib.PhoneLib
import nl.spindle.phonelib.model.Session
import nl.spindle.phonelib.repository.initialise.SessionCallback
import java.util.concurrent.TimeUnit

private const val REQUEST_MICROPHONE_PERMISSION = 2
private const val REQUEST_VIDEO_PERMISSION = 3

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private var latestSession: Session? = null

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
                latestSession = incomingSession
                PhoneLib.getInstance(this@MainActivity).setSpeaker(true)
                accept_call.visibility = View.VISIBLE
                hang_up.visibility = View.VISIBLE
                caller.text = incomingSession.getDisplayName
                setDuration()
            }

            override fun outgoingInit(session: Session) {
                super.outgoingInit(session)
                hang_up.visibility = View.VISIBLE
            }

            override fun sessionConnected(session: Session) {
                super.sessionConnected(session)
                PhoneLib.getInstance(this@MainActivity).setSpeaker(PhoneLib.getInstance(this@MainActivity).isVideoEnabled())
                PhoneLib.getInstance(this@MainActivity).setMuteMicrophone(false)
                accept_call.visibility = View.GONE
                call_buttons.visibility = View.VISIBLE
                call_data.visibility = View.VISIBLE
                caller.text = session.getDisplayName
                setDuration()
            }

            override fun sessionReleased(session: Session) {
                super.sessionReleased(session)
                sendBroadcast(Intent(VideoActivity.RECEIVE_FINISH_VIDEO_ACTIVITY))
                accept_call.visibility = View.GONE
                hang_up.visibility = View.GONE
                call_buttons.visibility = View.GONE
                call_data.visibility = View.GONE
                latestSession = null
            }

            override fun sessionEnded(session: Session) {
                super.sessionEnded(session)
                sendBroadcast(Intent(VideoActivity.RECEIVE_FINISH_VIDEO_ACTIVITY))
                accept_call.visibility = View.GONE
                hang_up.visibility = View.GONE
                call_buttons.visibility = View.GONE
                call_data.visibility = View.GONE
                latestSession = null
            }
        })
    }

    private fun setDuration() {
        handler = Handler();
        handler.postDelayed({
            duration.text = latestSession?.getDuration.toString()
            if (latestSession != null) {
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
                    latestSession = it
                }
            } else {
                Log.e(TAG, "No permission granted")
            }
        }
        video_call.setOnClickListener {
            val dialNum = dial_number.text.toString()
            if (hasVideoCallPermissions() && hasAudioCallPermissions()) {
                PhoneLib.getInstance(this@MainActivity).callTo(dialNum, true)?.let {
                    latestSession = it
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
            latestSession?.let {
                PhoneLib.getInstance(this@MainActivity).end(it)
            }
        }
        accept_call.setOnClickListener {
            if (hasAudioCallPermissions()) {
                latestSession?.let {
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
            PhoneLib.getInstance(this@MainActivity).setMuteMicrophone(checked)
        }
        toggle_speaker.setOnCheckedChangeListener{_, checked ->
            PhoneLib.getInstance(this@MainActivity).setSpeaker(checked)
        }
        toggle_hold.setOnCheckedChangeListener{_, checked ->
            latestSession?.let {
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
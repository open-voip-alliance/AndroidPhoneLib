package nl.spindle.phonelibexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_video.*
import nl.spindle.phonelib.PhoneLib

class VideoActivity : AppCompatActivity() {

    private var mReceiver: FinishVideoActivityReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val intentFilter = IntentFilter(RECEIVE_FINISH_VIDEO_ACTIVITY)
        mReceiver = FinishVideoActivityReceiver()
        registerReceiver(mReceiver, intentFilter)
        PhoneLib.getInstance(this).initialiseVideoViews(arrayOf(video_rendering), arrayOf(video_preview))
        setClickListeners()
    }

    private fun setClickListeners() {
        video_hang.setOnClickListener {
            finish()
        }

        video_mute.setOnClickListener {
            PhoneLib.getInstance(this).setMuteMicrophone(PhoneLib.getInstance(this).isMicrophoneMuted())
        }

        video_speaker.setOnClickListener {
            PhoneLib.getInstance(this).setSpeaker(PhoneLib.getInstance(this).isSpeakerOn())
        }
    }

    override fun onResume() {
        super.onResume()
        PhoneLib.getInstance(this).onResumeVideoCall()
    }

    override fun onPause() {
        super.onPause()
        PhoneLib.getInstance(this).onPauseVideoCall()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
        }
        PhoneLib.getInstance(this).onDestroyVideoCall()
    }

    inner class FinishVideoActivityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    companion object {
        const val RECEIVE_FINISH_VIDEO_ACTIVITY = "receive_finish_video_activity"
    }
}
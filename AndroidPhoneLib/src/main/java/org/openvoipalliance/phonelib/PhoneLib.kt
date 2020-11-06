package org.openvoipalliance.phonelib

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.view.SurfaceView
import androidx.annotation.RequiresPermission
import org.openvoipalliance.phonelib.di.Injection
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.model.Reason
import org.openvoipalliance.phonelib.model.AttendedTransferSession
import org.openvoipalliance.phonelib.presentation.call.video.SipVideoPresenter
import org.openvoipalliance.phonelib.repository.call.controls.SipActiveCallControlsRepository
import org.openvoipalliance.phonelib.repository.call.session.SipSessionRepository
import org.openvoipalliance.phonelib.repository.call.video.SipVideoCallRepository
import org.openvoipalliance.phonelib.repository.initialise.SessionCallback
import org.openvoipalliance.phonelib.repository.initialise.SipInitialiseRepository
import org.openvoipalliance.phonelib.repository.registration.RegistrationCallback
import org.openvoipalliance.phonelib.repository.registration.SipRegisterRepository
import org.koin.core.component.inject
import org.openvoipalliance.phonelib.config.Config

class PhoneLib private constructor(
        context: Context
) {
    private val injection = Injection(context)

    private val sipInitialiseRepository: SipInitialiseRepository by injection.inject()
    private val sipRegisterRepository: SipRegisterRepository by injection.inject()

    private val sipCallControlsRepository: SipActiveCallControlsRepository by injection.inject()
    private val sipSessionRepository: SipSessionRepository by injection.inject()
    private val sipVideoCallRepository: SipVideoCallRepository by injection.inject()

    private val sipVideoPresenter: SipVideoPresenter by injection.inject()

    /**
     * This needs to be called whenever this library needs to initialise. Without it, no other calls
     * can be done.
     */
    fun initialise(config: Config) = sipInitialiseRepository.initialise(config)

    /**
     * This registers your user on SIP. You need this before placing a call.
     */
    fun register(callback: RegistrationCallback) = sipRegisterRepository.register(callback)

    fun destroy() {
        unregister()
        sipInitialiseRepository.destroy()
    }

    /**
     * This unregisters your user on SIP.
     */
    fun unregister() = sipRegisterRepository.unregister()

    /**
     * Set a callback to listen to call changes where needed.
     * @param sessionCallback the session callback
     */
    fun setSessionCallback(sessionCallback: SessionCallback?) = sipInitialiseRepository.setSessionCallback(sessionCallback)

    /**
     * This method audio calls a phone number
     * @param number the number dialed to
     * @return returns true when call succeeds, false when the number is an empty string or the
     * phone service isn't ready.
     */
    @RequiresPermission(RECORD_AUDIO)
    fun callTo(number: String): Call? = sipSessionRepository.callTo(number)

    /**
     * This method calls a phone number, optionally with video
     * @param number the number dialed to
     * @param video whether you want to video dial or not
     * @return returns true when call succeeds, false when the number is an empty string or the
     * phone service isn't ready.
     */
    @RequiresPermission(allOf = [CAMERA, RECORD_AUDIO])
    fun callTo(number: String, video: Boolean): Call? = sipSessionRepository.callTo(number, video)


    /** --Control an incoming call-- */
    /**
     * Accepts the current incoming session/call when it exists
     */
    @RequiresPermission(RECORD_AUDIO)
    fun acceptIncoming(call: Call) = sipSessionRepository.acceptIncoming(call)

    /**
     * Declines the current incoming session/call when it exists
     */
    @RequiresPermission(RECORD_AUDIO)
    fun declineIncoming(call: Call, reason: Reason) = sipSessionRepository.declineIncoming(call, reason)

    /**
     * Hangs up the current active session/call when it exists
     */
    fun end(call: Call) = sipSessionRepository.end(call)


    /** --Controlling an active call-- */
    /**
     * Turns microphone off or on.
     * @param on If true, the microphone will turn off or stay off. If false it will turn on or stay on.
     */
    fun setMicrophone(on: Boolean) = sipCallControlsRepository.setMicrophone(on)

    /**
     * Transfer a session to a number unattended.
     * @param from The session you want to control.
     * @param to The number you want to call to.
     */
    fun transferUnattended(from: Call, to: String) = sipCallControlsRepository.transferUnattended(from, to)

    /**
     * Begin an attended transfer, putting the current call on hold and placing a call to a new user.
     *
     * @param from The session you want to control.
     * @param to The number you want to transfer to.
     */
    @RequiresPermission(allOf = [CAMERA, RECORD_AUDIO])
    fun beginAttendedTransfer(from: Call, to: String): AttendedTransferSession {
        pauseCall(from)

        val targetSession = callTo(to) ?: throw Exception("Unable to make call for target session")

        resumeCall(targetSession)

        return AttendedTransferSession(from, targetSession)
    }

    /**
     * Complete a pending attended transfer, merging the two calls.
     *
     * @param attendedTransferSession The transfer session that should be merged.
     */
    fun finishAttendedTransfer(attendedTransferSession: AttendedTransferSession) = sipCallControlsRepository.finishAttendedTransfer(attendedTransferSession)

    /**
     * Pause a session.
     * @param from The session you want to pause.
     */
    fun pauseCall(call: Call) = sipCallControlsRepository.pauseCall(call)

    /**
     * Resume a session.
     * @param call The session you want to resume.
     */
    fun resumeCall(call: Call) = sipCallControlsRepository.resumeCall(call)

    /**
     * Switch between sessions.
     * @param from The session you want to pause.
     * @param to The number you want to resume.
     */
    fun switchCall(from: Call, to: Call) = sipCallControlsRepository.switchCall(from, to)

    /**
     * Turns session on hold or off.
     * @param call The session you want to control.
     * @param on If true, hold will turn on or stay on. If false it will turn off or stay off.
     */
    fun setHold(call: Call, on: Boolean) = sipCallControlsRepository.setHold(call, on)


    /** --Get call states-- */
    /**
     * @return true if the mic is muted and false when it is not.
     */
    fun isMicrophoneMuted() = sipCallControlsRepository.isMicrophoneMuted()

    /**
     * Send a dtmf string.
     *
     */
    fun sendDtmf(call: Call, dtmf: String) =
        sipCallControlsRepository.sendDtmf(call, dtmf)

    /** --Video-- */
    /**
     * Needs to be initialise to display video calls.
     * @param renderingView to display the other parties' stream
     * @param previewView to display a preview of your camera
     */
    fun initialiseVideoViews(renderingView: Array<SurfaceView?>, previewView: Array<SurfaceView?>) =
            sipVideoPresenter.initialiseVideoViews(renderingView, previewView)

    /**
     * Determine if video is enabled for current active call
     * @return if video is enabled or not
     */
    fun isVideoEnabled() = sipVideoCallRepository.isVideoEnabled()

    /**
     * Needs to be called in onResume when using a video call
     */
    fun onResumeVideoCall() = sipVideoPresenter.onResume()

    /**
     * Needs to be called in onPause when using a video call
     */
    fun onPauseVideoCall() = sipVideoPresenter.onPause()

    /**
     * Needs to be called in onDestroy or when destroyed to end a video call
     */
    fun onDestroyVideoCall() = sipVideoPresenter.onDestroy()

    companion object {
        private var instance: PhoneLib? = null

        @JvmStatic
        fun getInstance(context: Context): PhoneLib {
            return instance ?: PhoneLib(context.applicationContext)
                    .also { instance = it }
        }
    }
}
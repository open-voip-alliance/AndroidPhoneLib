package nl.spindle.phonelib

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.view.SurfaceView
import androidx.annotation.RequiresPermission
import nl.spindle.phonelib.di.Injection
import nl.spindle.phonelib.model.Session
import nl.spindle.phonelib.model.Codec
import nl.spindle.phonelib.model.Reason
import nl.spindle.phonelib.presentation.call.video.SipVideoPresenter
import nl.spindle.phonelib.repository.call.codecs.SipCodecsRepository
import nl.spindle.phonelib.repository.call.controls.SipActiveCallControlsRepository
import nl.spindle.phonelib.repository.call.session.SipSessionRepository
import nl.spindle.phonelib.repository.call.video.SipVideoCallRepository
import nl.spindle.phonelib.repository.initialise.SessionCallback
import nl.spindle.phonelib.repository.initialise.SipInitialiseRepository
import nl.spindle.phonelib.repository.registration.RegistrationCallback
import nl.spindle.phonelib.repository.registration.SipRegisterRepository
import org.koin.core.inject

class PhoneLib private constructor(
        context: Context
) {
    private val injection = Injection(context)

    private val sipInitialiseRepository: SipInitialiseRepository by injection.inject()
    private val sipRegisterRepository: SipRegisterRepository by injection.inject()

    private val sipCodecsRepository: SipCodecsRepository by injection.inject()

    private val sipCallControlsRepository: SipActiveCallControlsRepository by injection.inject()
    private val sipSessionRepository: SipSessionRepository by injection.inject()
    private val sipVideoCallRepository: SipVideoCallRepository by injection.inject()

    private val sipVideoPresenter: SipVideoPresenter by injection.inject()

    /**
     * This needs to be called whenever this library needs to initialise. Without it, no other calls
     * can be done.
     * @param context the application context
     */
    fun initialise(context: Context) = sipInitialiseRepository.initialise(context)

    /**
     * This registers your user on SIP. You need this before placing a call.
     * @param username the SIP username
     * @param password the SIP password
     * @param domain the SIP server IP or host
     * @param port the SIP server port
     * @param registrationCallback the registration callback that will be called when registration succeeds or fails
     */
    fun register(username: String, password: String, domain: String, port: String, registrationCallback: RegistrationCallback)
            = sipRegisterRepository.registerUser(username, password, domain, port, registrationCallback)

    /**
     * Set the audio codecs you want to support, if none set all are selected, options are visible in @see nl.spindle.phonelib.model.Codec.
     * @param codecs the codecs you want to support
     */
    fun setAudioCodecs(context: Context, codecs: Set<Codec>)
            = sipCodecsRepository.setAudioCodecs(context, codecs)

    /**
     * Remove preferences for audio codecs and support all available codecs again @see nl.spindle.phonelib.model.Codec.
     */
    fun resetAudioCodecs(context: Context)
            = sipCodecsRepository.resetAudioCodecs(context)

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
    fun callTo(number: String): Session? = sipSessionRepository.callTo(number)

    /**
     * This method calls a phone number, optionally with video
     * @param number the number dialed to
     * @param video whether you want to video dial or not
     * @return returns true when call succeeds, false when the number is an empty string or the
     * phone service isn't ready.
     */
    @RequiresPermission(allOf = [CAMERA, RECORD_AUDIO])
    fun callTo(number: String, video: Boolean): Session? = sipSessionRepository.callTo(number, video)


    /**
     * Accepts the current incoming session/call when it exists
     */
    @RequiresPermission(RECORD_AUDIO)
    fun acceptIncoming(session: Session) = sipSessionRepository.acceptIncoming(session)
    
    /**
     * Declines the current incoming session/call when it exists
     */
    @RequiresPermission(RECORD_AUDIO)
    fun declineIncoming(session: Session, reason: Reason) = sipSessionRepository.declineIncoming(session, reason)

    /**
     * Hangs up the current active session/call when it exists
     */
    fun end(session: Session) = sipSessionRepository.end(session)


    /**
     * Turns microphone off or on.
     * @param on If true, the microphone will turn off or stay off. If false it will turn on or stay on.
     */
    fun setMuteMicrophone(on: Boolean) = sipCallControlsRepository.setMicrophone(on)

    /**
     * Turns speaker off or on.
     * @param on If true, the speaker will turn on or stay on. If false it will turn off or stay off.
     */
    fun setSpeaker(on: Boolean) = sipCallControlsRepository.setSpeaker(on)

    /**
     * Turns session on hold or off.
     * @param session The session you want to control.
     * @param on If true, the speaker will turn on or stay on. If false it will turn off or stay off.
     */
    fun setHold(session: Session, on: Boolean) = sipCallControlsRepository.setHold(session, on)


    /**
     * @return true if the mic is muted and false when it is not.
     */
    fun isMicrophoneMuted() = sipCallControlsRepository.isMicrophoneMuted()

    /**
     * @return true if the speaker is muted and false when it is not.
     */
    fun isSpeakerOn() = sipCallControlsRepository.isSpeakerEnabled()


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
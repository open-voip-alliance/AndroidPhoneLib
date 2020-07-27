package nl.spindle.phonelib.repository.call.video

import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager

internal class LinphoneSipVideoCallRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipVideoCallRepository {

    override fun isVideoEnabled(): Boolean {
        val remoteParams = linphoneCoreInstanceManager.safeLinphoneCore?.currentCall?.remoteParams
        return remoteParams != null && remoteParams.videoEnabled
    }

    override fun setVideoWindow(o: Any) {
        linphoneCoreInstanceManager.safeLinphoneCore?.setVideoWindow(o)
    }

    override fun removeVideoWindow() {
        linphoneCoreInstanceManager.safeLinphoneCore?.setVideoWindow(null)
    }

    override fun setPreviewWindow(o: Any?) {
        linphoneCoreInstanceManager.safeLinphoneCore?.setPreviewWindow(o)
    }

    override fun removePreviewWindow() {
        linphoneCoreInstanceManager.safeLinphoneCore?.setPreviewWindow(null)
    }
}
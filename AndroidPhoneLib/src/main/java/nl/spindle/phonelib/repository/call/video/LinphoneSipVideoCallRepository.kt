package nl.spindle.phonelib.repository.call.video

import nl.spindle.phonelib.repository.LinphoneCoreInstanceManager

internal class LinphoneSipVideoCallRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) : SipVideoCallRepository {

    override fun isVideoEnabled(): Boolean {
        //TODO: NOT IMPLEMENTED
        val remoteParams = linphoneCoreInstanceManager.safeLinphoneCore?.currentCall?.remoteParams
//        return remoteParams != null && remoteParams.videoEnabled
        return false
    }

    override fun setVideoWindow(o: Any) {
        //TODO: NOT IMPLEMENTED
//        linphoneCoreInstanceManager.safeLinphoneCore?.setVideoWindow(o)
    }

    override fun removeVideoWindow() {
        //TODO: NOT IMPLEMENTED
//        linphoneCoreInstanceManager.safeLinphoneCore?.setVideoWindow(null)
    }

    override fun setPreviewWindow(o: Any?) {
        //TODO: NOT IMPLEMENTED
//        linphoneCoreInstanceManager.safeLinphoneCore?.setPreviewWindow(o)
    }

    override fun removePreviewWindow() {
        //TODO: NOT IMPLEMENTED
//        linphoneCoreInstanceManager.safeLinphoneCore?.setPreviewWindow(null)
    }
}
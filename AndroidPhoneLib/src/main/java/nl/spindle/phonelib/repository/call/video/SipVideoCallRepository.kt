package nl.spindle.phonelib.repository.call.video

interface SipVideoCallRepository {
    fun isVideoEnabled(): Boolean

    fun setVideoWindow(o: Any)
    fun removeVideoWindow()
    fun setPreviewWindow(o: Any?)
    fun removePreviewWindow()
}
package nl.spindle.phonelib.presentation.call.video

import android.opengl.GLSurfaceView
import android.view.SurfaceView
import nl.spindle.phonelib.repository.call.video.SipVideoCallRepository
import org.linphone.mediastream.video.AndroidVideoWindowImpl

class LinphoneSipVideoPresenter(val sipVideoCallRepository: SipVideoCallRepository) : SipVideoPresenter {
    private var androidVideoWindow: AndroidVideoWindowImpl? = null
    private var renderingView: SurfaceView? = null
    private var previewView: SurfaceView? = null

    override fun initialiseVideoViews(renderingView: Array<SurfaceView?>, previewView: Array<SurfaceView?>) {
        this.renderingView = renderingView[0]
        this.previewView = previewView[0]
        fixZOrder(this.renderingView, this.previewView)
        androidVideoWindow = AndroidVideoWindowImpl(renderingView[0], previewView[0], object : AndroidVideoWindowImpl.VideoWindowListener {
            override fun onVideoRenderingSurfaceReady(androidVideoWindow: AndroidVideoWindowImpl, surfaceView: SurfaceView) {
                sipVideoCallRepository.setVideoWindow(androidVideoWindow)
                renderingView[0] = surfaceView
            }

            override fun onVideoRenderingSurfaceDestroyed(androidVideoWindow: AndroidVideoWindowImpl) {
                sipVideoCallRepository.removeVideoWindow()
            }

            override fun onVideoPreviewSurfaceReady(androidVideoWindow: AndroidVideoWindowImpl, surfaceView: SurfaceView) {
                this@LinphoneSipVideoPresenter.previewView = surfaceView
                sipVideoCallRepository.setPreviewWindow(this@LinphoneSipVideoPresenter.previewView)
            }

            override fun onVideoPreviewSurfaceDestroyed(androidVideoWindow: AndroidVideoWindowImpl) {
                sipVideoCallRepository.removePreviewWindow()
            }
        })
    }

    private fun fixZOrder(rendering: SurfaceView?, preview: SurfaceView?) {
        rendering?.setZOrderOnTop(false)
        preview?.setZOrderOnTop(true)
        preview?.setZOrderMediaOverlay(true)
    }

    override fun onResume() {
        if (renderingView != null) {
            (renderingView as GLSurfaceView?)!!.onResume()
        }
        if (androidVideoWindow != null) {
            synchronized(androidVideoWindow!!) { sipVideoCallRepository.setVideoWindow(androidVideoWindow!!) }
        }
    }

    override fun onPause() {
        if (androidVideoWindow != null) {
            synchronized(androidVideoWindow!!) { sipVideoCallRepository.removeVideoWindow() }
        }
        if (renderingView != null) {
            (renderingView as GLSurfaceView?)!!.onPause()
        }
    }

    override fun onDestroy() {
        previewView = null
        renderingView = null
        androidVideoWindow?.release()
        androidVideoWindow = null
    }
}
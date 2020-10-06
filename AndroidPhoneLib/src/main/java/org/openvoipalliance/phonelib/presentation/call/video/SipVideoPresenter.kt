package org.openvoipalliance.phonelib.presentation.call.video

import android.view.SurfaceView

interface SipVideoPresenter {
    fun initialiseVideoViews(renderingView: Array<SurfaceView?>, previewView: Array<SurfaceView?>)

    fun onResume()
    fun onPause()
    fun onDestroy()
}
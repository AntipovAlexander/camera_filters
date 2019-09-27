package com.antipov.camerafiltersidp

import android.view.SurfaceHolder

class SurfaceCreateCallback(
    private val onSurfaceChanged: (SurfaceHolder, Int, Int, Int) -> Unit = { surface, format, width, height -> },
    private val onSurfaceDestroyed: (SurfaceHolder) -> Unit = { surface -> },
    private val onSurfaceCreated: (SurfaceHolder) -> Unit = { surface -> }
) : SurfaceHolder.Callback {

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) =
        onSurfaceChanged(holder, format, width, height)

    override fun surfaceDestroyed(holder: SurfaceHolder) = onSurfaceDestroyed(holder)

    override fun surfaceCreated(holder: SurfaceHolder) = onSurfaceCreated(holder)
}
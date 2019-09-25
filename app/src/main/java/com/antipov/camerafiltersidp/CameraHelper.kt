package com.antipov.camerafiltersidp

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder

class CameraHelper(private val cameraManager: CameraManager, private val cameraId: String) :
    CameraDevice.StateCallback() {

    private var currentCamera: CameraDevice? = null
    private var surface = arrayListOf<Surface>()

    @SuppressLint("MissingPermission")
    fun openCamera() {
        cameraManager.openCamera(cameraId, this, null)
    }

    fun closeCamera() {
        currentCamera?.close()
    }

    fun isCameraOpened() = currentCamera == null

    fun configureSurfaces(surface: FixedAspectSurfaceView): Size? {
        // Find a good size for output - largest 16:9 aspect ratio that's less than 720p
        val MAX_WIDTH = 1280
        val TARGET_ASPECT = 16f / 9f
        val ASPECT_TOLERANCE = 0.1f

        val info = cameraManager.getCameraCharacteristics("0")

        val configs = info.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: throw RuntimeException("Cannot get available picture/preview sizes.")
        val outputSizes = configs.getOutputSizes(SurfaceHolder::class.java)

        var outputSize = outputSizes[0]
        var outputAspect = outputSize.getWidth().toFloat() / outputSize.getHeight()
        for (candidateSize in outputSizes) {
            if (candidateSize.getWidth() > MAX_WIDTH) continue
            val candidateAspect = candidateSize.getWidth().toFloat() / candidateSize.getHeight()
            val goodCandidateAspect = Math.abs(candidateAspect - TARGET_ASPECT) < ASPECT_TOLERANCE
            val goodOutputAspect = Math.abs(outputAspect - TARGET_ASPECT) < ASPECT_TOLERANCE
            if (goodCandidateAspect && !goodOutputAspect || candidateSize.getWidth() > outputSize.getWidth()) {
                outputSize = candidateSize
                outputAspect = candidateAspect
            }
        }

//         Configure the output view - this will fire surfaceChanged
        surface.setAspectRatio(outputAspect)
        surface.holder.setFixedSize(outputSize.width, outputSize.height)

        return outputSize
    }

    fun addSurface(s: Surface) {
//         todo: maybe there is needed to texture.setDefaultBufferSize(1920,1080);
        surface.add(s)
    }

    fun startPreview() {
        val builder = currentCamera?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
            surface.forEach { addTarget(it) }
        }
        currentCamera?.createCaptureSession(surface, object :
            CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {
                session.toString()
            }

            override fun onConfigured(session: CameraCaptureSession) {
                session.setRepeatingRequest(builder!!.build(), null, null)
            }

        }, null)

    }

    override fun onOpened(device: CameraDevice) {
        currentCamera = device

        startPreview()
    }

    override fun onDisconnected(device: CameraDevice) {
        closeCamera()
        currentCamera = null
    }

    override fun onError(p0: CameraDevice, p1: Int) {
        currentCamera = null
    }

}
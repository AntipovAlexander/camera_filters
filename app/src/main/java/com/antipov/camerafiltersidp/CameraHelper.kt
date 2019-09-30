package com.antipov.camerafiltersidp

import android.annotation.SuppressLint
import android.hardware.camera2.*
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder

class CameraHelper(
    private val cameraManager: CameraManager,
    private val cameraId: String
) :
    CameraDevice.StateCallback() {

    private var currentCamera: CameraDevice? = null
    private var currentSession: CameraCaptureSession? = null
    private lateinit var surface: Surface

    @SuppressLint("MissingPermission")
    fun openCamera() {
        cameraManager.openCamera(cameraId, this, null)
    }

    fun closeCamera() {
        currentSession?.close()
        currentSession = null
        currentCamera?.close()
        currentCamera = null
    }

    fun selectAppropriateSize(): Size {
        // Find a good size for output - largest 16:9 aspect ratio that's less than 720p
        val MAX_WIDTH = 1280
        val TARGET_ASPECT = 16f / 9f
        val ASPECT_TOLERANCE = 0.1f

        val info = cameraManager.getCameraCharacteristics("0")

        val configs = info.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: throw RuntimeException("Cannot get available picture/preview sizes.")
        val outputSizes = configs.getOutputSizes(SurfaceHolder::class.java)

        var outputSize = outputSizes[0]
        var outputAspect = outputSize.width.toFloat() / outputSize.height
        for (candidateSize in outputSizes) {
            if (candidateSize.width > MAX_WIDTH) continue
            val candidateAspect = candidateSize.width.toFloat() / candidateSize.height
            val goodCandidateAspect = Math.abs(candidateAspect - TARGET_ASPECT) < ASPECT_TOLERANCE
            val goodOutputAspect = Math.abs(outputAspect - TARGET_ASPECT) < ASPECT_TOLERANCE
            if (goodCandidateAspect && !goodOutputAspect || candidateSize.width > outputSize.width) {
                outputSize = candidateSize
                outputAspect = candidateAspect
            }
        }

        return outputSize
    }

    fun setSurface(s: Surface) {
        this.surface = s
    }

    fun startPreview() {
        val builder = currentCamera?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
            addTarget(surface)
        }
        currentCamera?.createCaptureSession(listOf(surface), object :
            CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {
                currentSession = session
            }

            override fun onConfigured(session: CameraCaptureSession) {
                currentSession = session
                session.setRepeatingRequest(builder!!.build(), null, null)
            }

        }, null)

    }

    override fun onOpened(device: CameraDevice) {
        currentCamera = device
//        try {
            startPreview()
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        } catch (e: IllegalArgumentException) {
//            e.printStackTrace()
//        }
    }

    override fun onDisconnected(device: CameraDevice) {
        currentCamera = device
        closeCamera()
    }

    override fun onError(device: CameraDevice, p1: Int) {
        currentCamera = device
        closeCamera()
    }

}
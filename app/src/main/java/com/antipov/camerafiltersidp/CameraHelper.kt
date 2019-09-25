package com.antipov.camerafiltersidp

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.view.Surface
import android.view.TextureView

class CameraHelper(private val cameraManager: CameraManager, private val cameraId: String) :
    CameraDevice.StateCallback() {

    private var currentCamera: CameraDevice? = null
    private lateinit var surface: Surface

    @SuppressLint("MissingPermission")
    fun openCamera() {
        cameraManager.openCamera(cameraId, this, null)
    }

    fun closeCamera() {
        currentCamera?.close()
    }

    fun isCameraOpened() = currentCamera == null

    fun setTextureView(cameraPreview: TextureView) {
        // todo: maybe there is needed to texture.setDefaultBufferSize(1920,1080);
        surface = Surface(cameraPreview.surfaceTexture)
    }

    fun startPreview() {
        val builder = currentCamera?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
            addTarget(surface)
        }
        currentCamera?.createCaptureSession(listOf(surface), object :
            CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {

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
        currentCamera = null
    }

    override fun onError(p0: CameraDevice, p1: Int) {
        currentCamera = null
    }

}
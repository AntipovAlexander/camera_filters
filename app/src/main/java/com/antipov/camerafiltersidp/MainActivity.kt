package com.antipov.camerafiltersidp

import android.content.Context
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraHelper: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // We fit the aspect ratio of TextureView to the size of preview we picked.
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraPreview1.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) = Unit

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) = Unit

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?) = false

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                cameraHelper = CameraHelper(cameraManager, "0")
                cameraHelper.setTextureView(cameraPreview1)
                cameraHelper.openCamera()
            }

        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun getCamerasList() {
        cameraManager.cameraIdList
    }
}

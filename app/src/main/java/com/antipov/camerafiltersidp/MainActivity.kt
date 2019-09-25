package com.antipov.camerafiltersidp

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.antipov.coroutines.idp_renderscript.ScriptC_bw
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var inputAllocation: Allocation
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraHelper: CameraHelper
    private lateinit var rs: RenderScript
    private lateinit var bwScript: ScriptC_bw

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // We fit the aspect ratio of TextureView to the size of preview we picked.

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraHelper = CameraHelper(cameraManager, "0")


        cameraPreview1.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                val size = cameraHelper.configureSurfaces(cameraPreview1)
                initRs(size!!.width, size!!.height)
                cameraHelper.addSurface(holder.surface)
                cameraHelper.addSurface(inputAllocation.surface)
                cameraHelper.openCamera()
            }

        })
    }

    private fun initRs(width: Int, height: Int) {
        rs = RenderScript.create(this)
        bwScript = ScriptC_bw(rs)
        val yuvTypeBuilder = Type.Builder(rs, Element.YUV(rs))
        yuvTypeBuilder.setX(width)
        yuvTypeBuilder.setY(height)
        yuvTypeBuilder.setYuvFormat(ImageFormat.YUV_420_888)
        inputAllocation = Allocation.createTyped(
            rs, yuvTypeBuilder.create(),
            Allocation.USAGE_IO_INPUT or Allocation.USAGE_SCRIPT
        )
        inputAllocation.setOnBufferAvailableListener {
            it.ioReceive()
            bwScript._in = inputAllocation
//            bwScript.forEach_identity(inputAllocation)
        }
    }
}

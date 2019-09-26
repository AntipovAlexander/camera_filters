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
import com.antipov.camerafiltersidp.pager.FiltersAdapter
import com.antipov.camerafiltersidp.pager.StackTransformer
import com.antipov.coroutines.idp_renderscript.ScriptC_bw
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var outputAllocation: Allocation
    private lateinit var inputAllocation: Allocation
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraHelper: CameraHelper
    private lateinit var rs: RenderScript
    private lateinit var bwScript: ScriptC_bw

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager.apply {
            adapter = FiltersAdapter(supportFragmentManager)
            setPageTransformer(true, StackTransformer())
            offscreenPageLimit = 1
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraHelper = CameraHelper(cameraManager, "0")

        cameraResult.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {

            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                val size = cameraHelper.configureSurfaces(cameraResult)
                initRs(size!!.width, size!!.height)
                cameraHelper.addSurface(inputAllocation.surface)
                cameraHelper.openCamera()
                outputAllocation.surface = holder.surface
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

        val rgbTypeBuilder = Type.Builder(rs, Element.RGBA_8888(rs))
        rgbTypeBuilder.setX(width)
        rgbTypeBuilder.setY(height)
        outputAllocation = Allocation.createTyped(
            rs, rgbTypeBuilder.create(),
            Allocation.USAGE_IO_OUTPUT or Allocation.USAGE_SCRIPT
        )

        ProcessingTask(inputAllocation, outputAllocation, bwScript)
    }
}

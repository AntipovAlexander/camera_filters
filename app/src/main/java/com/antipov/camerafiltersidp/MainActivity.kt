package com.antipov.camerafiltersidp

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.antipov.camerafiltersidp.filters.BlackAndWhiteFilter
import com.antipov.camerafiltersidp.filters.IdentityFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_bw
import com.antipov.coroutines.idp_renderscript.ScriptC_identity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var previewSize: Size
    private lateinit var outputAllocation: Allocation
    private lateinit var inputAllocation: Allocation
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraHelper: CameraHelper
    private lateinit var rs: RenderScript

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraHelper = CameraHelper(cameraManager, "0")
        previewSize = cameraHelper.configureSurfaces()

        scrollChoice.addItems(listOf("Original","Black & White","test 2","test 3","test 4","test 5","test 6"), 0)

        cameraResult.holder.addCallback(SurfaceCreateCallback(onSurfaceCreated = { holder ->
            initRs(previewSize.width, previewSize.height)
            cameraHelper.addSurface(inputAllocation.surface)
            cameraHelper.openCamera()
            outputAllocation.surface = holder.surface
            scrollChoice.selectedItemPosition = 0
            scrollChoice.setOnItemSelectedListener { scrollChoice, position, name ->
                when (position) {
                    0 -> {
                        val f =
                            IdentityFilter(inputAllocation, outputAllocation, ScriptC_identity(rs))
                        f.setup()
                    }
                    1 -> {
                        val f =
                            BlackAndWhiteFilter(inputAllocation, outputAllocation, ScriptC_bw(rs))
                        f.setup()
                    }
                }
            }
        }))
    }

    private fun initRs(width: Int, height: Int) {
        rs = RenderScript.create(this)
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
        val f = IdentityFilter(inputAllocation, outputAllocation, ScriptC_identity(rs))
                            f.setup()
    }
}

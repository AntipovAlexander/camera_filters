package com.antipov.camerafiltersidp

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import android.util.Size
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.antipov.camerafiltersidp.filters.FilterChanger
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var processingThread: HandlerThread
    private lateinit var previewSize: Size
    private lateinit var processingHandler: Handler
    private lateinit var outputAllocation: Allocation
    private lateinit var inputAllocation: Allocation
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraHelper: CameraHelper
    private lateinit var rs: RenderScript
    private lateinit var filterChanger: FilterChanger

    override fun onStart() {
        super.onStart()
        initProcessingThread()
        initCamera()
        initRs()
        initInputAllocation(previewSize.width, previewSize.height)
        initOutputAllocation(previewSize.width, previewSize.height)
        initFilterChanger()

    }

    override fun onResume() {
        super.onResume()
        setSurfaceCallback()
    }

    override fun onPause() {
        cameraHelper.closeCamera()
        stopBackgroundThread()
        cameraResult.holder.removeCallback(surfaceCallback)
        super.onPause()
    }

    private fun stopBackgroundThread() {
        processingThread.quitSafely()
        processingThread.join()
    }

    private fun initCamera() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraHelper = CameraHelper(cameraManager, "0")
        previewSize = cameraHelper.selectAppropriateSize()
    }

    private fun initProcessingThread() {
        processingThread = HandlerThread("Filter handler")
        processingThread.start()
        processingHandler = Handler(processingThread.looper)
    }

    private fun initRs() {
        rs = RenderScript.create(this)
    }

    private fun initInputAllocation(width: Int, height: Int) {
        val yuvTypeBuilder = Type.Builder(rs, Element.YUV(rs))
        yuvTypeBuilder.setX(width)
        yuvTypeBuilder.setY(height)
        yuvTypeBuilder.setYuvFormat(ImageFormat.YUV_420_888)
        inputAllocation = Allocation.createTyped(
            rs, yuvTypeBuilder.create(),
            Allocation.USAGE_IO_INPUT or Allocation.USAGE_SCRIPT
        )
    }

    private fun initOutputAllocation(width: Int, height: Int) {
        val rgbTypeBuilder = Type.Builder(rs, Element.RGBA_8888(rs))
        rgbTypeBuilder.setX(width)
        rgbTypeBuilder.setY(height)
        outputAllocation = Allocation.createTyped(
            rs, rgbTypeBuilder.create(),
            Allocation.USAGE_IO_OUTPUT or Allocation.USAGE_SCRIPT
        )
    }

    private fun initFilterChanger() {
        filterChanger = FilterChanger(inputAllocation, outputAllocation, processingHandler, rs)
        filterChanger.init()
        filterChanger.setupWithSelector(scrollChoice)
    }

    private fun setSurfaceCallback() {
        cameraResult.holder.addCallback(surfaceCallback)
    }

    private val surfaceCallback: SurfaceCreateCallback =
        SurfaceCreateCallback(onSurfaceChanged = ::runCamera)

    private fun runCamera(holder: SurfaceHolder, a: Int, b: Int, c: Int) {
        cameraHelper.setSurface(inputAllocation.surface)
        outputAllocation.surface = holder.surface
        cameraHelper.openCamera()
    }

    override fun onStop() {
        rs.finish()
        rs.destroy()
        inputAllocation.destroy()
        outputAllocation.destroy()
        filterChanger.destroy()
        super.onStop()
    }
}

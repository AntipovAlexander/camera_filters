package com.antipov.camerafiltersidp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.antipov.camerafiltersidp.CameraHelper
import com.antipov.camerafiltersidp.R
import com.antipov.camerafiltersidp.SurfaceCreateCallback
import com.antipov.camerafiltersidp.filters.AbstractFilter
import com.antipov.camerafiltersidp.filters.FilterChanger
import kotlinx.android.synthetic.main.fragment_camera.*

class CameraFragment : Fragment(), AbstractFilter.FpsListener {

    private lateinit var processingThread: HandlerThread
    private lateinit var previewSize: Size
    private lateinit var processingHandler: Handler
    private lateinit var outputAllocation: Allocation
    private lateinit var inputAllocation: Allocation
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraHelper: CameraHelper
    private lateinit var rs: RenderScript
    private lateinit var filterChanger: FilterChanger

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View.inflate(context, R.layout.fragment_camera, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCamera()
        initRs()
        initFilterChanger()
    }

    override fun onStart() {
        super.onStart()
        initInputAllocation(previewSize.width, previewSize.height)
        initOutputAllocation(previewSize.width, previewSize.height)
    }

    override fun onResume() {
        super.onResume()
        initProcessingThread()
        if (cameraResult.holder.surface.isValid)
            runCamera(cameraResult.holder)
        else
            setSurfaceCallback()
    }

    override fun onPause() {
        cameraHelper.closeCamera()
        stopBackgroundThread()
        cameraResult.holder.removeCallback(surfaceCallback)
        filterChanger.stopFpsCounter()
        super.onPause()
    }

    private fun stopBackgroundThread() {
        processingThread.quitSafely()
        processingThread.join()
    }

    private fun initCamera() {
        cameraManager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraHelper = CameraHelper(cameraManager, "0")
        previewSize = cameraHelper.selectAppropriateSize()
    }

    private fun initProcessingThread() {
        processingThread = HandlerThread("Filter handler")
        processingThread.start()
        processingHandler = Handler(processingThread.looper)
    }

    private fun initRs() {
        rs = RenderScript.create(context)
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
        filterChanger = FilterChanger(rs, this)
        filterChanger.setupWithSelector(scrollChoice)
    }

    private fun setSurfaceCallback() {
        cameraResult.holder.addCallback(surfaceCallback)
    }

    private val surfaceCallback: SurfaceCreateCallback =
        SurfaceCreateCallback(onSurfaceChanged = ::runCamera)

    private fun runCamera(holder: SurfaceHolder) {
        cameraHelper.setSurface(inputAllocation.surface)
        outputAllocation.surface = holder.surface
        cameraHelper.openCamera()
        filterChanger.startFiltering(inputAllocation, outputAllocation, processingHandler)
    }

    @SuppressLint("SetTextI18n")
    override fun onFpsUpdated(fps: Int) {
        fpsView?.post { fpsView?.text = "Fps: $fps" }
    }

    override fun onDestroyView() {
        filterChanger.destroy()
        inputAllocation.destroy()
        outputAllocation.destroy()
        rs.finish()
        rs.destroy()
        super.onDestroyView()
    }
}
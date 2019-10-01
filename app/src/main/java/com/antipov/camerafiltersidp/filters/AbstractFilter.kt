package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import android.renderscript.ScriptC
import java.util.*

abstract class AbstractFilter(
    val inputAllocation: Allocation,
    val outputAllocation: Allocation,
    val processingHandler: Handler,
    val fpsListener: FpsListener? = null,
    private val script: ScriptC
) : Runnable, Allocation.OnBufferAvailableListener {

    private var pendingFrames = 0
    private var overallFrames: Int = 0

    private val fpsTimer: Timer = Timer()
    private val fpsCountTask: TimerTask = object : TimerTask() {
        override fun run() {
            fpsListener?.onFpsUpdated(overallFrames)
            overallFrames = 0
        }
    }

    fun setup() {
        fpsTimer.schedule(fpsCountTask, 0, 1000L)
        inputAllocation.setOnBufferAvailableListener(this)
    }

    override fun onBufferAvailable(buffer: Allocation) {
        synchronized(this) {
            pendingFrames++
            processingHandler.post(this)
        }
    }

    override fun run() {
        // Find out how many frames have arrived
        val arrivedFrames: Int
        synchronized(this) {
            arrivedFrames = pendingFrames
            pendingFrames = 0
            overallFrames++
            // Discard extra messages in case processing is slower than frame rate
            processingHandler.removeCallbacks(this)
        }

        // Get to newest input
        for (i in 0 until arrivedFrames) inputAllocation.ioReceive()

        performFiltering(inputAllocation, outputAllocation)
        outputAllocation.ioSend()
    }

    abstract fun performFiltering(inputAllocation: Allocation, outputAllocation: Allocation)

    fun cancelFpsCount() = fpsTimer.cancel()

    fun destroy() = script.destroy()

    interface FpsListener {
        fun onFpsUpdated(fps: Int)
    }
}
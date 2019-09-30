package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import android.renderscript.ScriptC

abstract class AbstractFilter(
    private val inputAllocation: Allocation,
    private val outputAllocation: Allocation,
    private val processingHandler: Handler,
    private val script: ScriptC
) : Runnable, Allocation.OnBufferAvailableListener {

    private var pendingFrames = 0

    abstract val name: String

    fun setup() = inputAllocation.setOnBufferAvailableListener(this)

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

            // Discard extra messages in case processing is slower than frame rate
            processingHandler.removeCallbacks(this)
        }

        // Get to newest input
        for (i in 0 until arrivedFrames) {
            inputAllocation.ioReceive()
        }

        performFiltering(inputAllocation, outputAllocation)

        outputAllocation.ioSend()
    }

    abstract fun performFiltering(inputAllocation: Allocation, outputAllocation: Allocation)

    fun destroy() {
        script.destroy()
    }
}
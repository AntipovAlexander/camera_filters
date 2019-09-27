package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.os.HandlerThread
import android.renderscript.Allocation

abstract class AbstractFilter(
    private val inputAllocation: Allocation,
    private val outputAllocation: Allocation
) : Runnable, Allocation.OnBufferAvailableListener {
    private lateinit var processingHandler: Handler
    private var pendingFrames = 0

    fun setup() {
        inputAllocation.setOnBufferAvailableListener(this)
        val processingThread = HandlerThread(javaClass.canonicalName)
        processingThread.start()
        processingHandler = Handler(processingThread.looper)
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
        processingHandler.removeCallbacks(this)
        inputAllocation.setOnBufferAvailableListener(null)
    }
}
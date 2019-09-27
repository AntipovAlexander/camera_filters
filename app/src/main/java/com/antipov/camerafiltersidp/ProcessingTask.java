package com.antipov.camerafiltersidp;

import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import com.antipov.coroutines.idp_renderscript.ScriptC_bw;

class ProcessingTask implements Runnable, Allocation.OnBufferAvailableListener {

    private int mPendingFrames = 0;
    private final Handler mProcessingHandler;
    private Allocation mInputAllocation;
    private Allocation mOutputAllocation;
    private ScriptC_bw mScript;

    public ProcessingTask(Allocation input, Allocation output, ScriptC_bw script) {
        mInputAllocation = input;
        mOutputAllocation = output;
        mScript = script;
        mInputAllocation.setOnBufferAvailableListener(this);
        HandlerThread processingThread = new HandlerThread("ViewfinderProcessor");
        processingThread.start();
        mProcessingHandler = new Handler(processingThread.getLooper());
    }

    @Override
    public void onBufferAvailable(Allocation a) {
        synchronized (this) {
            mPendingFrames++;
            mProcessingHandler.post(this);
        }
    }

    @Override
    public void run() {

        // Find out how many frames have arrived
        int pendingFrames;
        synchronized (this) {
            pendingFrames = mPendingFrames;
            mPendingFrames = 0;

            // Discard extra messages in case processing is slower than frame rate
            mProcessingHandler.removeCallbacks(this);
        }

        // Get to newest input
        for (int i = 0; i < pendingFrames; i++) {
            mInputAllocation.ioReceive();
        }

        mScript.set_in(mInputAllocation);
        mScript.forEach_root(mOutputAllocation);
        mOutputAllocation.ioSend();
    }
}
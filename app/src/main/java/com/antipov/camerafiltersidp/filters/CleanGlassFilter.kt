package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import com.antipov.coroutines.idp_renderscript.ScriptC_CleanGlassFilter

class CleanGlassFilter(
    inputAllocation: Allocation,
    outputAllocation: Allocation,
    processingHandler: Handler,
    listener: FpsListener?,
    private val scriptC: ScriptC_CleanGlassFilter
) : AbstractFilter(inputAllocation, outputAllocation, processingHandler, listener, scriptC) {

    override fun performFiltering(inputAllocation: Allocation, outputAllocation: Allocation) {
        scriptC._in = inputAllocation
        scriptC.invoke_setup()
        scriptC.forEach_root(outputAllocation)
    }
}
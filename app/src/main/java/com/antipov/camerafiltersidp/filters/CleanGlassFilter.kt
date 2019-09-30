package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import com.antipov.coroutines.idp_renderscript.ScriptC_CleanGlassFilter

class CleanGlassFilter(
    inputAllocation: Allocation,
    outputAllocation: Allocation,
    processingHandler: Handler,
    private val scriptC: ScriptC_CleanGlassFilter
) : AbstractFilter(inputAllocation, outputAllocation, processingHandler) {

    override val name: String = "Clean Glass"

    override fun performFiltering(inputAllocation: Allocation, outputAllocation: Allocation) {
        scriptC._in = inputAllocation
        scriptC.invoke_setup()
        scriptC.forEach_root(outputAllocation)
    }
}
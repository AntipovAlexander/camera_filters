package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import com.antipov.coroutines.idp_renderscript.ScriptC_identity

class IdentityFilter(
    inputAllocation: Allocation,
    outputAllocation: Allocation,
    processingHandler: Handler,
    private val scriptC: ScriptC_identity
) : AbstractFilter(inputAllocation, outputAllocation, processingHandler, scriptC) {

    override val name: String = "Original"

    override fun performFiltering(inputAllocation: Allocation, outputAllocation: Allocation) {
        scriptC._in = inputAllocation
        scriptC.forEach_root(outputAllocation)
    }
}
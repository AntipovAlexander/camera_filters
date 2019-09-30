package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import com.antipov.coroutines.idp_renderscript.ScriptC_BrickFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_bw

class BrickFilter(
    inputAllocation: Allocation,
    outputAllocation: Allocation,
    processingHandler: Handler,
    private val scriptC: ScriptC_BrickFilter
) : AbstractFilter(inputAllocation, outputAllocation, processingHandler) {

    override val name: String = "Brick"

    override fun performFiltering(inputAllocation: Allocation, outputAllocation: Allocation) {
        scriptC._in = inputAllocation
        scriptC.forEach_root(outputAllocation)
    }
}
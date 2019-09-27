package com.antipov.camerafiltersidp.filters

import android.renderscript.Allocation
import com.antipov.coroutines.idp_renderscript.ScriptC_bw

class BlackAndWhiteFilter(
    inputAllocation: Allocation,
    outputAllocation: Allocation,
    private val scriptC: ScriptC_bw
) : AbstractFilter(inputAllocation, outputAllocation) {

    override fun performFiltering(inputAllocation: Allocation, outputAllocation: Allocation) {
        scriptC._in = inputAllocation
        scriptC.forEach_root(outputAllocation)
    }
}
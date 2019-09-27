package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import cn.louispeng.imagefilter.renderscript.ScriptC_ReliefFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_CleanGlassFilter

class ReliefFilter(
    inputAllocation: Allocation,
    outputAllocation: Allocation,
    processingHandler: Handler,
    private val scriptC: ScriptC_ReliefFilter
) : AbstractFilter(inputAllocation, outputAllocation, processingHandler) {

    override fun performFiltering(inputAllocation: Allocation, outputAllocation: Allocation) {
        scriptC._in = inputAllocation
        scriptC.invoke_setup()
        scriptC.forEach_root(outputAllocation)
    }
}
package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import cn.louispeng.imagefilter.renderscript.ScriptC_BrightContrastFilter

class BrightContrastFilter(
    inputAllocation: Allocation,
    outputAllocation: Allocation,
    processingHandler: Handler,
    listener: FpsListener?,
    private val scriptC: ScriptC_BrightContrastFilter
) : AbstractFilter(inputAllocation, outputAllocation, processingHandler, listener, scriptC) {

    override fun performFiltering(inputAllocation: Allocation, outputAllocation: Allocation) {
        scriptC._in = inputAllocation
        scriptC.invoke_setup()
        scriptC.forEach_root(outputAllocation)
    }
}
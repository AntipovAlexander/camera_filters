package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import android.renderscript.ScriptC
import cn.louispeng.imagefilter.renderscript.ScriptC_ReliefFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_BrickFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_CleanGlassFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_bw
import com.antipov.coroutines.idp_renderscript.ScriptC_identity

class FilterFactory {

    companion object {
        fun create(
            script: ScriptC,
            input: Allocation,
            output: Allocation,
            handler: Handler,
            listener: AbstractFilter.FpsListener? = null
        ): AbstractFilter {
            return when (script) {
                is ScriptC_identity -> IdentityFilter(
                    input,
                    output,
                    handler,
                    listener,
                    script
                )
                is ScriptC_bw -> BlackAndWhiteFilter(
                    input,
                    output,
                    handler,
                    listener,
                    script
                )
                is ScriptC_BrickFilter -> BrickFilter(
                    input,
                    output,
                    handler,
                    listener,
                    script
                )
                is ScriptC_CleanGlassFilter -> CleanGlassFilter(
                    input,
                    output,
                    handler,
                    listener,
                    script
                )
                is ScriptC_ReliefFilter -> ReliefFilter(
                    input,
                    output,
                    handler,
                    listener,
                    script
                )
                else -> throw RuntimeException("Unknown ScriptC")
            }
        }
    }

}
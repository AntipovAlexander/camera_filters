package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import android.renderscript.RenderScript
import cn.louispeng.imagefilter.renderscript.ScriptC_ReliefFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_BrickFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_CleanGlassFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_bw
import com.antipov.coroutines.idp_renderscript.ScriptC_identity
import com.webianks.library.scroll_choice.ScrollChoice

class FilterChanger(
    private val renderScript: RenderScript
) {

    private var currentFilter: AbstractFilter? = null
    private var lastFilter: Int = 0

    private fun makeFilter(
        inputAllocation: Allocation,
        outputAllocation: Allocation,
        processingHandler: Handler,
        position: Int
    ): AbstractFilter {
        return when (position) {
            0 -> IdentityFilter(
                inputAllocation,
                outputAllocation,
                processingHandler,
                ScriptC_identity(renderScript)
            )
            1 -> BlackAndWhiteFilter(
                inputAllocation,
                outputAllocation,
                processingHandler,
                ScriptC_bw(renderScript)
            )
            2 -> BrickFilter(
                inputAllocation,
                outputAllocation,
                processingHandler,
                ScriptC_BrickFilter(renderScript)
            )
            3 -> CleanGlassFilter(
                inputAllocation,
                outputAllocation,
                processingHandler,
                ScriptC_CleanGlassFilter(renderScript)
            )
            4 -> ReliefFilter(
                inputAllocation,
                outputAllocation,
                processingHandler,
                ScriptC_ReliefFilter(renderScript)
            )
            else -> throw RuntimeException("Wrong filter position")
        }
    }

    fun setupWithSelector(scrollChoice: ScrollChoice) {
        scrollChoice.addItems(
            listOf(
                "1",
                "2",
                "3",
                "4"
            ), 0
        )
        scrollChoice.setOnItemSelectedListener { _, position, _ ->
            lastFilter = position
            currentFilter = currentFilter?.let { nonNullFilter ->
                makeFilter(
                    nonNullFilter.inputAllocation,
                    nonNullFilter.outputAllocation,
                    nonNullFilter.processingHandler,
                    lastFilter
                )
            }
            currentFilter?.setup()
        }
    }

    fun startFiltering(
        inputAllocation: Allocation,
        outputAllocation: Allocation,
        processingHandler: Handler
    ) {
        currentFilter?.destroy()
        currentFilter = makeFilter(
            inputAllocation,
            outputAllocation,
            processingHandler,
            lastFilter
        )
        currentFilter?.setup()
    }

    fun destroy() {
        currentFilter?.destroy()
    }
}
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
import java.util.*

class FilterChanger(private val renderScript: RenderScript) {

    private var currentFilter: AbstractFilter? = null
    private var lastFilter: Int = 0

    private var filtersMap: LinkedHashMap<Int, String> = linkedMapOf(
        0 to "Original",
        1 to "Black & White",
        2 to "Brick",
        3 to "Clean glass",
        4 to "Relief"
    )

    private fun makeFilter(input: Allocation, output: Allocation, handler: Handler, position: Int): AbstractFilter {
        return when (position) {
            0 -> FilterFactory.create(ScriptC_identity(renderScript), input, output, handler)
            1 -> FilterFactory.create(ScriptC_bw(renderScript), input, output, handler)
            2 -> FilterFactory.create(ScriptC_BrickFilter(renderScript), input, output, handler)
            3 -> FilterFactory.create(ScriptC_CleanGlassFilter(renderScript), input, output, handler)
            4 -> FilterFactory.create(ScriptC_ReliefFilter(renderScript), input, output, handler)
            else -> throw RuntimeException("Wrong filter position")
        }
    }

    fun setupWithSelector(scrollChoice: ScrollChoice) {
        scrollChoice.addItems(filtersMap.values.toList(), 0)
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

    fun startFiltering(inputAllocation: Allocation, outputAllocation: Allocation, processingHandler: Handler) {
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
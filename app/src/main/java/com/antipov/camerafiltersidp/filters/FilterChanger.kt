package com.antipov.camerafiltersidp.filters

import android.os.Handler
import android.renderscript.Allocation
import android.renderscript.RenderScript
import cn.louispeng.imagefilter.renderscript.ScriptC_BigBrother
import cn.louispeng.imagefilter.renderscript.ScriptC_BrightContrastFilter
import cn.louispeng.imagefilter.renderscript.ScriptC_ColorQuantizeFilter
import cn.louispeng.imagefilter.renderscript.ScriptC_ReliefFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_BrickFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_CleanGlassFilter
import com.antipov.coroutines.idp_renderscript.ScriptC_bw
import com.antipov.coroutines.idp_renderscript.ScriptC_identity
import com.webianks.library.scroll_choice.ScrollChoice
import java.util.*

class FilterChanger(
    private val renderScript: RenderScript,
    private val listener: AbstractFilter.FpsListener
) : ScrollChoice.OnItemSelectedListener {

    private var currentFilter: AbstractFilter? = null
    private var lastFilter: Int = 0

    private var filtersMap: LinkedHashMap<Int, String> = linkedMapOf(
        0 to "Original",
        1 to "Black & White",
        2 to "Brick",
        3 to "Clean glass",
        4 to "Relief",
        5 to "Big Brother",
        6 to "Bright contrast",
        7 to "Color quantizer"
    )

    private fun makeFilter(input: Allocation, output: Allocation, handler: Handler, position: Int): AbstractFilter {
        return when (position) {
            0 -> FilterFactory.create(ScriptC_identity(renderScript), input, output, handler, listener)
            1 -> FilterFactory.create(ScriptC_bw(renderScript), input, output, handler, listener)
            2 -> FilterFactory.create(ScriptC_BrickFilter(renderScript), input, output, handler, listener)
            3 -> FilterFactory.create(ScriptC_CleanGlassFilter(renderScript), input, output, handler, listener)
            4 -> FilterFactory.create(ScriptC_ReliefFilter(renderScript), input, output, handler, listener)
            5 -> FilterFactory.create(ScriptC_BigBrother(renderScript), input, output, handler, listener)
            6 -> FilterFactory.create(ScriptC_BrightContrastFilter(renderScript), input, output, handler, listener)
            7 -> FilterFactory.create(ScriptC_ColorQuantizeFilter(renderScript), input, output, handler, listener)
            else -> throw RuntimeException("Wrong filter position")
        }
    }

    fun setupWithSelector(scrollChoice: ScrollChoice) {
        scrollChoice.addItems(filtersMap.values.toList(), 0)
        scrollChoice.setOnItemSelectedListener(this)
    }

    override fun onItemSelected(scrollChoice: ScrollChoice?, position: Int, name: String?) {
        lastFilter = position
        currentFilter?.cancelFpsCount()
        currentFilter = currentFilter?.let { filter ->
            makeFilter(filter.inputAllocation, filter.outputAllocation, filter.processingHandler, lastFilter)
        }
        currentFilter?.setup()
    }

    fun startFiltering(input: Allocation, output: Allocation, handler: Handler) {
        currentFilter?.destroy()
        currentFilter = makeFilter(input, output, handler, lastFilter)
        currentFilter?.setup()
    }

    fun destroy() {
        currentFilter?.destroy()
        stopFpsCounter()
    }

    fun stopFpsCounter() = currentFilter?.cancelFpsCount()
}
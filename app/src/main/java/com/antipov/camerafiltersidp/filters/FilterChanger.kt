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
    inputAllocation: Allocation,
    outputAllocation: Allocation,
    processingHandler: Handler,
    renderScript: RenderScript
) {

    private lateinit var currentFilter: AbstractFilter

    private val filters = arrayListOf(
        IdentityFilter(
            inputAllocation,
            outputAllocation,
            processingHandler,
            ScriptC_identity(renderScript)
        ),
        BlackAndWhiteFilter(
            inputAllocation,
            outputAllocation,
            processingHandler,
            ScriptC_bw(renderScript)
        ),
        BrickFilter(
            inputAllocation,
            outputAllocation,
            processingHandler,
            ScriptC_BrickFilter(renderScript)
        ),
        CleanGlassFilter(
            inputAllocation,
            outputAllocation,
            processingHandler,
            ScriptC_CleanGlassFilter(renderScript)
        ),
        ReliefFilter(
            inputAllocation,
            outputAllocation,
            processingHandler,
            ScriptC_ReliefFilter(renderScript)
        )
    )

    init {
        currentFilter = filters[0]
        currentFilter.setup()
    }

    fun setupWithSelector(scrollChoice: ScrollChoice) {
        scrollChoice.addItems(
            listOf(
                filters[0].name,
                filters[1].name,
                filters[2].name,
                filters[3].name,
                filters[4].name
            ), 0
        )
        scrollChoice.setOnItemSelectedListener { _, position, _ ->
            filters[position].setup()
        }
    }
}
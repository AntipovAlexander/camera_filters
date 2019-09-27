package com.antipov.camerafiltersidp.filters

import com.webianks.library.scroll_choice.ScrollChoice

class FilterChanger {
    private val filters = arrayListOf<AbstractFilter>(
    )

    fun setupWithSelector(scrollChoice: ScrollChoice) {
        scrollChoice.addItems(
            listOf(
                "Original",
                "Black & White",
                "test 2",
                "test 3",
                "test 4",
                "test 5",
                "test 6"
            ), 0
        )
        scrollChoice.setOnItemSelectedListener { scrollChoice, position, name ->

        }
    }
}
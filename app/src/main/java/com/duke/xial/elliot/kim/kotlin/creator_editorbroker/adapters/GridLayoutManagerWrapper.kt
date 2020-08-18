package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager

@Suppress("unused")
class GridLayoutManagerWrapper: GridLayoutManager {
    constructor(context: Context, spanCount: Int) : super(context, spanCount)
    constructor(context: Context, spanCount: Int, orientation: Int, reverseLayout: Boolean) :
            super(context, spanCount, orientation, reverseLayout)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)
    override fun supportsPredictiveItemAnimations(): Boolean { return false }
}
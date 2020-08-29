package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs

class MovableFloatingActionButton : FloatingActionButton, View.OnTouchListener {
    private val childButtons = arrayListOf<MovableFloatingActionButton>()
    private val distanceDifferencesFromParent = arrayListOf<Pair<Float, Float>>()
    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f
    private var newX = 0F
    private var newY = 0F
    var isMovable = true

    constructor(context: Context) : super(context) { init() }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init() }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        setOnTouchListener(this)
    }

    fun setChildButtons(vararg buttons: MovableFloatingActionButton) {
        for(button in buttons) {
            childButtons.add(button)
        }
    }

    private fun calculateDistanceFromParent(parentX: Float, parentY: Float) {
        for (button in childButtons)
            distanceDifferencesFromParent.add(Pair(abs(button.x - parentX), button.y - parentY))
    }

    private fun setChildButtonsCoordinate() {
        for ((index, button) in childButtons.withIndex()) {
            button.x = this.x - distanceDifferencesFromParent[index].first
            button.y = this.y + distanceDifferencesFromParent[index].second
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val layoutParams = view.layoutParams as MarginLayoutParams
        val action = motionEvent.action
        return if (action == MotionEvent.ACTION_DOWN) {
            println("POPOPOPOPOPOPOPO")
            downRawX = motionEvent.rawX
            downRawY = motionEvent.rawY
            dX = view.x - downRawX
            dY = view.y - downRawY
            calculateDistanceFromParent(view.x, view.y)
            true
        } else if (action == MotionEvent.ACTION_MOVE) {
            val viewWidth: Int = view.width
            val viewHeight: Int = view.height
            val viewParent: View = view.parent as View
            val parentWidth: Int = viewParent.width
            val parentHeight: Int = viewParent.height

            if (isMovable) {
                newX = motionEvent.rawX + dX
                newX = layoutParams.leftMargin.toFloat().coerceAtLeast(newX)
                newX = (parentWidth - viewWidth - layoutParams.rightMargin.toFloat()).coerceAtMost(newX)
                newY = motionEvent.rawY + dY
                newY = layoutParams.topMargin.toFloat().coerceAtLeast(newY)
                newY = (parentHeight - viewHeight - layoutParams.bottomMargin.toFloat()).coerceAtMost(newY)

                view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start()
            }
            true
        } else if (action == MotionEvent.ACTION_UP) {
            val upRawX = motionEvent.rawX
            val upRawY = motionEvent.rawY
            val upDX = upRawX - downRawX
            val upDY = upRawY - downRawY
            if (abs(upDX) < CLICK_DRAG_TOLERANCE && abs(upDY) < CLICK_DRAG_TOLERANCE) {
                performClick()
            } else {
                setChildButtonsCoordinate()
                true
            }
        } else {
            super.onTouchEvent(motionEvent)
        }
    }

    companion object {
        private const val CLICK_DRAG_TOLERANCE = 16F
    }
}
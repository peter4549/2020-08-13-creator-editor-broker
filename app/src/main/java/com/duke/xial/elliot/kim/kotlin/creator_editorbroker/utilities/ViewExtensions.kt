package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities

import android.animation.Animator
import android.view.View

fun View.scaleDown(duration: Long = 200L) {
    this.animate()
        .scaleX(0.0F)
        .scaleY(0.0F)
        .alpha(0F)
        .setDuration(duration)
        .setListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(animator: Animator?) {  }
            override fun onAnimationEnd(animator: Animator?) {
                this@scaleDown.visibility = View.INVISIBLE
            }
            override fun onAnimationCancel(animator: Animator?) {  }
            override fun onAnimationRepeat(animator: Animator?) {  }
        })
        .start()
}

fun View.scaleUp(scale: Float = 0.8F, duration: Long = 200L) {
    this.animate()
        .scaleX(scale)
        .scaleY(scale)
        .alpha(1F)
        .setDuration(duration)
        .setListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(animator: Animator?) {
                this@scaleUp.visibility = View.VISIBLE
            }
            override fun onAnimationEnd(animator: Animator?) {  }
            override fun onAnimationCancel(animator: Animator?) {  }
            override fun onAnimationRepeat(animator: Animator?) {  }
        })
        .start()
}

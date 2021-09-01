package com.innerCat.pillBox.factories

import android.app.Activity
import android.content.SharedPreferences
import android.view.View
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.innerCat.pillBox.R
import com.innerCat.pillBox.room.Converters
import kotlin.math.ceil
import kotlin.math.max

fun showTapTarget(activity: Activity, view: View, minRadius: Int, title: String = "", description: String = "", onClick: (() -> Unit)? = null, onLongClick: (() -> Unit)? = null, onCancel: (() -> Unit)? = null) {
    TapTargetView.showFor(activity,  // `this` is an Activity
            TapTarget.forView(view, title, description).apply {// All options below are optional
                outerCircleColor(R.color.primaryColor) // Specify a color for the outer circle
                outerCircleAlpha(0.96f) // Specify the alpha amount for the outer circle
                targetCircleColor(R.color.transparent) // Specify a color for the target circle
                titleTextSize(20) // Specify the size (in sp) of the title text
                titleTextColor(R.color.white) // Specify the color of the title text
                //                            .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                dimColor(R.color.black) // If set, will dim behind the view with 30% opacity of the given color
                drawShadow(true) // Whether to draw a drop shadow or not
                cancelable(true) // Whether tapping outside the outer circle dismisses the view
                transparentTarget(true) // Specify whether the target is transparent (displays the content underneath)
                targetRadius(max(minRadius, ceil(Converters.fromPixelsToDp((max(view.width, view.height).toDouble()/1.5).toInt(), activity.resources).toDouble()).toInt()))  // Specify the target radius (in dp)
            },
            object : TapTargetView.Listener() {
                // The listener can listen for regular clicks, long clicks or cancels
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view) // This call is optional
                    if (onClick != null) {
                        onClick()
                    }
                }
                override fun onTargetLongClick(view: TapTargetView?) {
                    super.onTargetLongClick(view)
                    if (onLongClick != null) {
                        onLongClick()
                    }
                }

                override fun onTargetCancel(view: TapTargetView) {
                    super.onTargetCancel(view)
                    if (onCancel != null) {
                        onCancel()
                    }
                }

            })

}

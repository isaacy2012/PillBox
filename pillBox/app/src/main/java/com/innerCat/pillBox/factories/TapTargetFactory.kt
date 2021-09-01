package com.innerCat.pillBox.factories

import android.app.Activity
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.View
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.innerCat.pillBox.R
import com.innerCat.pillBox.activities.MainActivity
import com.innerCat.pillBox.databinding.MainActivityBinding
import com.innerCat.pillBox.room.Converters
import kotlin.math.ceil
import kotlin.math.max

class TapTargetFactory(private val activity: MainActivity) {

    private val g: MainActivityBinding
        get() {
            return activity.g
        }

    private val sharedPreferences: SharedPreferences
        get() {
            return activity.sharedPreferences
        }

    private val editButtonView: View
        get() {
            return activity.editButtonView
        }

    fun start() {
        showFABTapTarget()
    }

    private fun setOnboardingDone() {
        val edit: SharedPreferences.Editor = sharedPreferences.edit()
        edit.putBoolean(activity.getString(R.string.sp_should_show_onboarding), false)
        edit.apply()
    }

    /**
     * Show fab tap target.
     */
    private fun showFABTapTarget() {
        showTapTarget(activity, g.fab, 80, "Add your first item", "Click outside at any point to cancel the tutorial.",
                {
                    g.fab.callOnClick()
                },
                null,
                {
                    setOnboardingDone()
                })
    }

    /**
     * Return to tap target after adding an item, showing how to decrement
     */
    fun showRVItemTapTargetDecrement() {
        val handler = Handler(Looper.getMainLooper())
        val activity: Activity = activity
        handler.postDelayed(
                {
                    val view: View? = g.rvItems.getChildAt(0)
                    if (view != null) {
                        showTapTarget(activity, view, 100, "Click on an item to decrement it", "Long press to view refills.", {
                            showRVItemTapTargetRefill()
                        }, null,
                                {
                                    setOnboardingDone()
                                }
                        )
                    }
                }, activity.getResources().getInteger(R.integer.rv_animation_duration)
                .toLong())
    }

    /**
     * Show refill tap target
     */
    private fun showRVItemTapTargetRefill() {
        val activity: Activity = activity
        val view: View = g.rvItems.getChildAt(0) ?: return
        val refillView = view.findViewById<View>(R.id.refillButton)
                ?: return
        showTapTarget(activity, refillView, 15, "Refill your item", "Add a date to remember expiry.", {
            showEditTapTarget()
        }, null,
                {
                    setOnboardingDone()
                }
        )
    }


    /**
     * Show edit tap target
     */
    private fun showEditTapTarget() {
        showTapTarget(activity,
                editButtonView,
                10,
                "Edit items",
                "You can edit or rearrange your items",
                {
                    editButtonView.callOnClick()
                    showRVItemTapTargetEdit()
                },
                null,
                null)
    }

    /**
     * Show RVItem Tap target edit
     */
    private fun showRVItemTapTargetEdit() {
        val view: View? = g.rvItems.getChildAt(0)
        if (view != null) {
            showTapTarget(activity, view, 100, "Click to edit your item", "Drag to rearrange items.", {
                showEditTapTargetBack()
            }, null,
                    {
                        setOnboardingDone()
                    }
            )
        }
    }

    /**
     * Final tap target
     */
    private fun showEditTapTargetBack() {
        val onboardingDoneFun = { setOnboardingDone() }
        showTapTarget(activity,
                editButtonView,
                10,
                "That's all! Welcome to pillBox",
                "Click here to go back.",
                {
                    setOnboardingDone()
                    editButtonView.callOnClick()
                },
                onboardingDoneFun,
                onboardingDoneFun)
    }


}

fun showTapTarget(activity: Activity, view: View, minRadius: Int, title: String = "", description: String = "", onClick: (() -> Unit)? = null, onLongClick: (() -> Unit)? = null, onCancel: (() -> Unit)? = null) {
    TapTargetView.showFor(activity,  // `activity` is an Activity
            TapTarget.forView(view, title, description).apply {// All options below are optional
                outerCircleColor(R.color.primaryColor) // Specify a color for the outer circle
                outerCircleAlpha(0.96f) // Specify the alpha amount for the outer circle
                targetCircleColor(R.color.transparent) // Specify a color for the target circle
                titleTextSize(20) // Specify the size (in sp) of the title text
                titleTextColor(R.color.white) // Specify the color of the title text
                descriptionTextAlpha(0.75f)
                //                            .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                dimColor(R.color.black) // If set, will dim behind the view with 30% opacity of the given color
                drawShadow(true) // Whether to draw a drop shadow or not
                cancelable(true) // Whether tapping outside the outer circle dismisses the view
                transparentTarget(true) // Specify whether the target is transparent (displays the content underneath)
                targetRadius(max(minRadius, ceil(Converters.fromPixelsToDp((max(view.width, view.height).toDouble() / 1.5).toInt(), activity.resources).toDouble()).toInt()))  // Specify the target radius (in dp)
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

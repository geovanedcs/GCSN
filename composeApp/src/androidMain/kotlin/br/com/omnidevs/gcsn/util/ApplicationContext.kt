package br.com.omnidevs.gcsn.util

import android.app.Activity
import java.lang.ref.WeakReference

object ApplicationContext {
    private var activityRef: WeakReference<Activity>? = null

    val activity: Activity?
        get() = activityRef?.get()

    fun initialize(activity: Activity) {
        activityRef = WeakReference(activity)
    }
}
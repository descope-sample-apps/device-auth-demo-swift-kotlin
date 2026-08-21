package com.descope.deviceauthdemo.util

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration

/**
 * True when running on Android TV / Google TV (a Leanback launcher device),
 * as opposed to a phone or tablet. Used to pick between the touch-oriented
 * Compose Material3 screens and the D-pad-oriented `tv-material` screens —
 * both drive the exact same [com.descope.deviceauthdemo.viewmodel.DeviceAuthViewModel].
 */
fun Context.isTelevision(): Boolean {
    val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
}

package com.descope.deviceauthdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.descope.deviceauthdemo.ui.DeviceAuthApp
import com.descope.deviceauthdemo.ui.theme.DeviceAuthDemoTheme
import com.descope.deviceauthdemo.ui.tv.TvDeviceAuthApp
import com.descope.deviceauthdemo.ui.tv.TvDeviceAuthDemoTheme
import com.descope.deviceauthdemo.util.isTelevision
import androidx.tv.material3.Surface as TvSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Same DeviceAuthViewModel/state machine either way — only the
            // screens (touch vs. D-pad-focused) differ. See ui/tv/ for the
            // Android TV / Google TV variant.
            if (isTelevision()) {
                TvDeviceAuthDemoTheme {
                    TvSurface(modifier = Modifier.fillMaxSize()) {
                        TvDeviceAuthApp()
                    }
                }
            } else {
                DeviceAuthDemoTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        DeviceAuthApp()
                    }
                }
            }
        }
    }
}

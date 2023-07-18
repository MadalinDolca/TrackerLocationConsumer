package com.madalin.trackerlocationconsumer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.madalin.trackerlocationconsumer.ui.screen.MainScreen
import com.madalin.trackerlocationconsumer.ui.theme.TrackerLocationConsumerTheme

class MainActivity() : ComponentActivity()/*, AndroidScopeComponent*/ {

    //override val scope: Scope by activityScope() //activityRetainedScope()
    //private val hello by inject<String>() // without qualifier
    //private val hello by inject<String>(named("hello")) // with qualifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackerLocationConsumerTheme {
                MainScreen()
            }
        }
    }
}
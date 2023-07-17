package com.madalin.trackerlocationconsumer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.madalin.trackerlocationconsumer.feature.login.ui.LoginScreen
import com.madalin.trackerlocationconsumer.ui.theme.TrackerLocationConsumerTheme
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class MainActivity() : ComponentActivity()/*, AndroidScopeComponent*/ {

    //override val scope: Scope by activityScope() //activityRetainedScope()
    //private val hello by inject<String>() // without qualifier
    //private val hello by inject<String>(named("hello")) // with qualifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackerLocationConsumerTheme {
                LoginScreen()
            }
        }
    }
}
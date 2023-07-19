package com.madalin.trackerlocationconsumer.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph(start = true)
@NavGraph
annotation class AppNavGraph(
    val start: Boolean = true
)
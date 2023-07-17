package com.madalin.trackerlocationconsumer.di

import com.madalin.trackerlocationconsumer.MainActivity
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Set of dependencies that will only live as long as the activity they are injected into.
 */
val activityModule = module {
    // scoped to MainActivity
    scope<MainActivity> {
        scoped { "Hello" } // string scoped to MainActivity without qualifier

        // multiple strings scoped to MainActivity with qualifier
        scoped(qualifier = named("hello")) { "Hello" }
        scoped(qualifier = named("hi")) { "Hi" }
    }
}
package com.madalin.trackerlocationconsumer.di

import com.madalin.trackerlocationconsumer.entity.ApplicationStateDriver
import com.madalin.trackerlocationconsumer.feature.login.LoginViewModel
import com.madalin.trackerlocationconsumer.feature.tracker.ui.TrackerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ApplicationStateDriver() } // ApplicationStateDriver dependency as a singleton
    viewModel { LoginViewModel(get()) } // injects LoginViewModel with the above dependency
    viewModel { TrackerViewModel(get()) }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get()) } // injects LoginViewModel with the above dependency
    viewModel { TrackerViewModel(get()) }
}

package com.madalin.trackerlocationconsumer.di

import com.madalin.trackerlocationconsumer.model.AppStateDriver
import com.madalin.trackerlocationconsumer.feature.auth.LoginViewModel
import com.madalin.trackerlocationconsumer.feature.auth.SignUpViewModel
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppStateDriver() } // ApplicationStateDriver dependency as a singleton
}

val viewModelModule = module {
    viewModel { LoginViewModel(get()) } // injects LoginViewModel with the above dependency
    viewModel { SignUpViewModel() }
    viewModel { TrackerViewModel(get()) }
}

package com.madalin.trackerlocationconsumer.di

import com.madalin.trackerlocationconsumer.feature.auth.login.LoginViewModel
import com.madalin.trackerlocationconsumer.feature.auth.signup.SignUpViewModel
import com.madalin.trackerlocationconsumer.feature.tracker.TrackerViewModel
import com.madalin.trackerlocationconsumer.model.AppStateDriver
import com.madalin.trackerlocationconsumer.repository.FirebaseRepositoryImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppStateDriver(get()) } // ApplicationStateDriver dependency as a singleton
    single { FirebaseRepositoryImpl() }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get(), get()) } // injects LoginViewModel with the above dependencies
    viewModel { SignUpViewModel(get()) }
    viewModel { TrackerViewModel(get(), get()) }
}

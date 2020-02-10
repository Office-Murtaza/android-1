package com.app.belcobtm.data.di

import android.preference.PreferenceManager
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import org.koin.dsl.module

val dataModule = module {
    single {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(get())
        SharedPreferencesHelper(sharedPreferences)
    }
}
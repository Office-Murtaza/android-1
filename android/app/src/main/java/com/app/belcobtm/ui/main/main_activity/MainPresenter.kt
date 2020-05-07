package com.app.belcobtm.ui.main.main_activity

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.mvp.BaseMvpPresenterImpl


class MainPresenter : BaseMvpPresenterImpl<MainContract.View>(),
    MainContract.Presenter {
    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    override fun checkPinEntered() {
        when {
            prefsHelper.accessToken.isEmpty() -> mView?.onTokenNotSaved()
            prefsHelper.apiSeed.isEmpty() -> mView?.onSeedNotSaved()
            prefsHelper.userPin.isNotBlank() -> mView?.onPinSaved()
            else -> mView?.onPinNotSaved()
        }
    }
}
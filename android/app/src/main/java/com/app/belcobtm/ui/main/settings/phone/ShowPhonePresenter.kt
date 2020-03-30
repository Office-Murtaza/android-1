package com.app.belcobtm.ui.main.settings.phone

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.SettingsDataManager
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.ui.main.coins.settings.phone.ShowPhoneContract


class ShowPhonePresenter : BaseMvpDIPresenterImpl<ShowPhoneContract.View, SettingsDataManager>(),
    ShowPhoneContract.Presenter {
    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun attachView(view: ShowPhoneContract.View) {
        super.attachView(view)

        val userId = prefsHelper.userId.toString()
        mView?.showProgress(true)
        mDataManager.getPhone(userId).subscribe(
            { response ->
                mView?.showProgress(false)
                mView?.onPhoneReceived(response.value?.phone)

            },
            { error ->
                checkError(error)
            })
    }
}
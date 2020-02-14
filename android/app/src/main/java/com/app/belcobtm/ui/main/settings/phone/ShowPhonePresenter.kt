package com.app.belcobtm.ui.main.settings.phone

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.SettingsDataManager
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.ui.main.coins.settings.phone.ShowPhoneContract
import com.app.belcobtm.core.pref


class ShowPhonePresenter : BaseMvpDIPresenterImpl<ShowPhoneContract.View, SettingsDataManager>(),
    ShowPhoneContract.Presenter {
    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun attachView(view: ShowPhoneContract.View) {
        super.attachView(view)

        val userId = App.appContext().pref.getUserId().toString()
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
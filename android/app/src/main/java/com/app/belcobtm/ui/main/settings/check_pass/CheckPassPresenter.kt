package com.app.belcobtm.ui.main.settings.check_pass

import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.SettingsDataManager
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.ui.main.coins.settings.check_pass.CheckPassContract
import com.app.belcobtm.util.pref


class CheckPassPresenter : BaseMvpDIPresenterImpl<CheckPassContract.View, SettingsDataManager>(),
    CheckPassContract.Presenter {
    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun checkPass(pass: String) {
        val userId = App.appContext().pref.getUserId().toString()
        mView?.showProgress(true)
        mDataManager.checkPass(userId, pass).subscribe(
            { response ->
                mView?.showProgress(false)
                if (response.value != null && response.value!!.match) {
                    mView?.onPassConfirmed()
                } else {
                    mView?.showMessage(R.string.password_doesnt_match)
                }
            },
            { error ->
                mView?.showProgress(false)
                mView?.showMessage(error.message)
            })
    }

    override fun requestSeed() {
        val seed = App.appContext().pref.getSeed()
        mView?.onSeedGot(seed)
    }
}
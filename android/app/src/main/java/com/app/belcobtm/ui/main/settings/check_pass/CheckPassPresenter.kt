package com.app.belcobtm.ui.main.settings.check_pass

import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.SettingsDataManager
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.ui.main.coins.settings.check_pass.CheckPassContract
import com.app.belcobtm.presentation.core.pref


class CheckPassPresenter : BaseMvpDIPresenterImpl<CheckPassContract.View, SettingsDataManager>(),
    CheckPassContract.Presenter {

    private val mUserId = App.appContext().pref.getUserId().toString()

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun checkPass(pass: String) {
        mView?.showProgress(true)
        mDataManager.checkPass(mUserId, pass).subscribe(
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
        mView?.onSeedReceived(seed)
    }

    override fun updatePhone(phone: String) {
        mView?.showProgress(true)
        mDataManager.updatePhone(mUserId, phone).subscribe(
            { response ->
                mView?.showProgress(false)
                if (response.value != null && response.value!!.smsSent) {
                    mView?.openSmsCodeDialog()
                } else {
                    mView?.showMessage(R.string.password_doesnt_match)
                }
            },
            { error ->
                mView?.showProgress(false)
                mView?.showMessage(error.message)
            })
    }

    override fun confirmPhoneSms(phone: String, code: String) {
        mDataManager.confirmPhoneSms(mUserId, phone, code).subscribe(
            { response ->
                mView?.showProgress(false)
                if (response.value != null && response.value!!.confirmed) {
                    mView?.onSmsConfirmed()
                } else {
                    mView?.showMessage(R.string.password_doesnt_match)
                }
            },
            { error ->
                checkError(error)
            })
    }

    override fun unlink() {
        mDataManager.unlink(mUserId).subscribe(
            { response ->
                mView?.showProgress(false)
                if (response.value != null && response.value!!.updated) {
                    mView?.onUnlinkSuccess()

                } else {
                    mView?.showMessage(R.string.password_doesnt_match)
                }
            },
            { error ->
                checkError(error)
            })
    }


}
package com.app.belcobtm.ui.main.settings.change_pass

import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.SettingsDataManager
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.ui.main.coins.settings.change_pass.ChangePassContract
import com.app.belcobtm.util.pref


class ChangePassPresenter : BaseMvpDIPresenterImpl<ChangePassContract.View, SettingsDataManager>(),
    ChangePassContract.Presenter {

    private val mUserId = App.appContext().pref.getUserId().toString()

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun changePass(oldPass: String, newPass: String, confirmNewPass: String) {
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmNewPass.isEmpty()) {
            mView?.showError(R.string.error_all_fields_required)
        } else if (newPass.length < 6) {
            mView?.showError(R.string.error_short_pass)
        } else if (newPass != confirmNewPass) {
            mView?.showError(R.string.error_confirm_pass)
        } else {
            mView?.showProgress(true)
            mDataManager.changePass(mUserId, oldPass, newPass).subscribe(
                { response ->
                    mView?.showProgress(false)
                    if (response.value != null && response.value!!.updated) {
                        mView?.onPassChanged()
                    } else {
                        mView?.showMessage(R.string.password_doesnt_match)
                    }
                },
                { error ->
                    checkError(error)
                })
        }
    }

    override fun changePin(oldPin: String, newPin: String, confirmNewPin: String) {
        val savedPin = App.appContext().pref.getPin()
        if (oldPin.isEmpty() || newPin.isEmpty() || confirmNewPin.isEmpty()) {
            mView?.showError(R.string.error_all_fields_required)
        } else if (oldPin != savedPin) {
            mView?.showError(R.string.error_wrong_old_pin)
        } else if (newPin.length < 6) {
            mView?.showError(R.string.error_short_pin)
        } else if (newPin != confirmNewPin) {
            mView?.showError(R.string.error_confirm_pin)
        } else {
            App.appContext().pref.setPin(newPin)
            mView?.onPinChanged()
        }
    }

}
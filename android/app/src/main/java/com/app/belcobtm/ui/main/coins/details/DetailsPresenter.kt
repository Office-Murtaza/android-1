package com.app.belcobtm.ui.main.coins.details

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl


class DetailsPresenter : BaseMvpDIPresenterImpl<DetailsContract.View, WithdrawDataManager>(),
    DetailsContract.Presenter {

    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun getDetails(coinCode: String, transactionId: String, transactionDbId: String) {
        mDataManager.getTransactionDetails(
            prefsHelper.userId.toString(),
            coinCode,
            transactionId,
            transactionDbId
        ).subscribe({ response ->
            if (response.value?.txId != null || response.value?.txDbId != null) {
                mView?.showTransactionDetails(response.value)
            }
        }, { error ->
            checkError(error)
        })
    }
}
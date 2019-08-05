package com.app.belcobtm.ui.main.atm

import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.model.response.AtmResponse
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.Optional


class AtmPresenter : BaseMvpDIPresenterImpl<AtmContract.View, CoinsDataManager>(),
    AtmContract.Presenter {

    override val atmAddressList: ArrayList<AtmResponse.AtmAddress> = arrayListOf()

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun requestAtmAddressList() {
        mView?.showProgress(true)
        mDataManager.getAtmAddress().subscribe(
            { response: Optional<AtmResponse> ->
                mView?.showProgress(false)

                atmAddressList.clear()
                atmAddressList.addAll(response.value!!.atmAddressList)

                mView?.notifyAtmAddressList()
            }
            , { error: Throwable ->
                checkError(error)
            })
    }

}
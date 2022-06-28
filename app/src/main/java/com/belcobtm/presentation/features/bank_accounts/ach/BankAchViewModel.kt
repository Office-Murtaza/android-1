package com.belcobtm.presentation.features.bank_accounts.ach

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.domain.bank_account.interactor.GetLinkTokenUseCase
import com.belcobtm.domain.bank_account.interactor.LinkBankAccountUseCase
import com.belcobtm.domain.bank_account.item.BankAccountLinkDataItem
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData

class BankAchViewModel(
    private val getLinkTokenUseCase: GetLinkTokenUseCase,
    private val linkBankAccountUseCase: LinkBankAccountUseCase,
    prefHelper: SharedPreferencesHelper
) : ViewModel() {

    private val _linkToken = SingleLiveData<LoadingData<String>>()
    val linkToken: LiveData<LoadingData<String>> = _linkToken

    private val _consumerName = SingleLiveData<String>()
    val consumerName: LiveData<String> = _consumerName

    init {
        _consumerName.value = if (prefHelper.userFirstName.isNotEmpty())
            "${prefHelper.userFirstName} ${prefHelper.userLastName}"
        else CONSUMER_NAME
    }

    fun getLinkToken() {
        _linkToken.value = LoadingData.Loading()
        getLinkTokenUseCase.invoke(Unit,
            onSuccess = {
                _linkToken.value = LoadingData.Success(
                    it
                )
            },
            onError = {
            }
        )
    }

    fun linkBankAccounts(linkBankAccountDataItem: BankAccountLinkDataItem) {
        linkBankAccountUseCase.invoke(
            LinkBankAccountUseCase.Params(linkBankAccountDataItem),
            onSuccess = {},
            onError = {}
        )
    }

    companion object {

        private const val CONSUMER_NAME = "CONSUMER NAME"
        const val USDC_TERMS_LINK = "https://www.circle.com/en/legal/usdc-terms"
    }

}

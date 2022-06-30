package com.belcobtm.presentation.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.bank_account.interactor.ConnectToBankAccountsUseCase
import com.belcobtm.domain.bank_account.interactor.ConnectToPaymentsUseCase
import com.belcobtm.domain.service.ConnectToServicesUseCase
import com.belcobtm.domain.socket.ConnectToSocketUseCase
import com.belcobtm.domain.trade.list.ConnectToOrdersDataUseCase
import com.belcobtm.domain.trade.list.ConnectToTradesDataUseCase
import com.belcobtm.domain.trade.order.ConnectToChatUseCase
import com.belcobtm.domain.transaction.interactor.ConnectToTransactionsUseCase
import com.belcobtm.domain.wallet.interactor.ConnectToWalletUseCase

class MainViewModel(
    private val connectToSocketUseCase: ConnectToSocketUseCase,
    private val connectToWalletUseCase: ConnectToWalletUseCase,
    private val connectToBankAccountsUseCase: ConnectToBankAccountsUseCase,
    private val connectToPaymentsUseCase: ConnectToPaymentsUseCase,
    private val connectToTransactionsUseCase: ConnectToTransactionsUseCase,
    private val connectToServicesUseCase: ConnectToServicesUseCase,
    private val connectToTradesDataUseCase: ConnectToTradesDataUseCase,
    private val connectToOrdersDataUseCase: ConnectToOrdersDataUseCase,
    private val connectToChatUseCase: ConnectToChatUseCase,
) : ViewModel() {

    fun connectToWebSockets() {
        Log.d("WEB_SOCKET", "MASS SUBSCRIPTION)")
        connectToSocketUseCase(Unit, onSuccess = {
            connectToWalletUseCase(Unit)
            connectToTransactionsUseCase(Unit)
            connectToServicesUseCase(Unit)
            connectToBankAccountsUseCase(Unit)
            connectToPaymentsUseCase(Unit)
            connectToTradesDataUseCase(Unit)
            connectToOrdersDataUseCase(Unit)
            connectToChatUseCase(Unit)
        })
    }

}

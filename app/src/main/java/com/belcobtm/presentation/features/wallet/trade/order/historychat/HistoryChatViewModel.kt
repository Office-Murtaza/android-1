package com.belcobtm.presentation.features.wallet.trade.order.historychat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.order.GetChatHistoryUseCase
import com.belcobtm.presentation.core.adapter.model.ListItem
import kotlinx.coroutines.Dispatchers

class HistoryChatViewModel(
    private val getChatHistoryUseCase: GetChatHistoryUseCase
) : ViewModel() {

    fun getChatHistory(orderId: String): LiveData<Either<Failure, List<ListItem>>> =
        getChatHistoryUseCase(orderId)
            .asLiveData(Dispatchers.Default)
}
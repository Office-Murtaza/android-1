package com.app.belcobtm.presentation.features.wallet.trade.order.historychat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.trade.order.GetChatHistoryUseCase
import com.app.belcobtm.presentation.core.adapter.model.ListItem

class HistoryChatViewModel(
    private val getChatHistoryUseCase: GetChatHistoryUseCase
) : ViewModel() {

    private var _chatContent = MutableLiveData<List<ListItem>>()
    val chatContent: LiveData<List<ListItem>> = _chatContent

    fun loadChatHistory(orderId: Int) {
        getChatHistoryUseCase(orderId, onSuccess = _chatContent::setValue)
    }
}
package com.app.belcobtm.presentation.features.wallet.trade.order.chat

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.trade.order.ConnectToChatUseCase
import com.app.belcobtm.domain.trade.order.DisconnectFromChatUseCase
import com.app.belcobtm.domain.trade.order.SendChatMessageUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class OrderChatViewModel(
    private val connectToChatUseCase: ConnectToChatUseCase,
    private val disconnectFromChatUseCase: DisconnectFromChatUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase
) : ViewModel() {

    private val _chatObserverLoadingData = MutableLiveData<LoadingData<Unit>>()
    val chatObserverLoadingData: LiveData<LoadingData<Unit>> = _chatObserverLoadingData

    private val _attachmentImage = MutableLiveData<Bitmap?>()
    val attachmentImage: LiveData<Bitmap?> = _attachmentImage

    fun connectToChat() {
        connectToChatUseCase.invoke(Unit)
    }

    fun disconnectFromChat() {
        disconnectFromChatUseCase.invoke(Unit)
    }

    fun sendMessage(orderId: Int, myId: Int, toId: Int, message: String) {
        val attachment = _attachmentImage.value
        setAttachment(null)
        _chatObserverLoadingData.value = LoadingData.Loading()
        sendChatMessageUseCase(
            NewMessageItem(orderId, myId, toId, message, attachment),
            onSuccess = { _chatObserverLoadingData.value = LoadingData.Success(Unit) },
            onError = { _chatObserverLoadingData.value = LoadingData.Error(it) }
        )
    }

    fun setAttachment(attachment: Bitmap?) {
        _attachmentImage.value = attachment
    }
}
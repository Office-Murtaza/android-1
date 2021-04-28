package com.app.belcobtm.presentation.features.wallet.trade.order.chat

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.app.belcobtm.domain.trade.order.ConnectToChatUseCase
import com.app.belcobtm.domain.trade.order.DisconnectFromChatUseCase
import com.app.belcobtm.domain.trade.order.ObserveChatMessagesUseCase
import com.app.belcobtm.domain.trade.order.SendChatMessageUseCase
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.Dispatchers

class OrderChatViewModel(
    private val connectToChatUseCase: ConnectToChatUseCase,
    private val disconnectFromChatUseCase: DisconnectFromChatUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val observeChatUseCase: ObserveChatMessagesUseCase
) : ViewModel() {

    private val _chatObserverLoadingData = MutableLiveData<LoadingData<Unit>>()
    val chatObserverLoadingData: LiveData<LoadingData<Unit>> = _chatObserverLoadingData

    private val _attachmentImage = MutableLiveData<Bitmap?>()
    val attachmentImage: LiveData<Bitmap?> = _attachmentImage

    private var attachmentName: String? = null

    fun chatData(orderId: String): LiveData<List<ListItem>> =
        observeChatUseCase(orderId)
            .asLiveData(Dispatchers.IO)

    fun connectToChat() {
        connectToChatUseCase.invoke(Unit)
    }

    fun disconnectFromChat() {
        disconnectFromChatUseCase.invoke(Unit)
    }

    fun sendMessage(orderId: String, myId: Int, toId: Int, message: String) {
        val attachment = _attachmentImage.value
        setAttachment(null, null)
        _chatObserverLoadingData.value = LoadingData.Loading()
        sendChatMessageUseCase(
            NewMessageItem(orderId, myId, toId, message, attachmentName, attachment),
            onSuccess = { _chatObserverLoadingData.value = LoadingData.Success(Unit) },
            onError = { _chatObserverLoadingData.value = LoadingData.Error(it) }
        )
    }

    fun setAttachment(uri: Uri?, attachment: Bitmap?) {
        _attachmentImage.value = attachment
        attachmentName = uri?.lastPathSegment.orEmpty()
    }
}
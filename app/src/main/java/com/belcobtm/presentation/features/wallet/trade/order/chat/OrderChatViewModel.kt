package com.belcobtm.presentation.features.wallet.trade.order.chat

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.belcobtm.domain.trade.order.ObserveChatMessagesUseCase
import com.belcobtm.domain.trade.order.SendChatMessageUseCase
import com.belcobtm.domain.trade.order.UpdateLastSeenMessageTimeStampUseCase
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.Dispatchers
import java.util.*

class OrderChatViewModel(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val observeChatUseCase: ObserveChatMessagesUseCase,
    private val updateLastSeenMessageTimeStampUseCase: UpdateLastSeenMessageTimeStampUseCase
) : ViewModel() {

    private val _chatObserverLoadingData = MutableLiveData<LoadingData<Unit>>()
    val chatObserverLoadingData: LiveData<LoadingData<Unit>> = _chatObserverLoadingData

    private val _attachmentImage = MutableLiveData<Bitmap?>()
    val attachmentImage: LiveData<Bitmap?> = _attachmentImage

    private var attachmentName: String? = null

    fun chatData(orderId: String): LiveData<List<ListItem>> =
        observeChatUseCase(orderId)
            .asLiveData(Dispatchers.Default)

    fun updateTimestamp() {
        updateLastSeenMessageTimeStampUseCase(Unit)
    }

    fun sendMessage(orderId: String, myId: String, toId: String, message: String) {
        val attachment = _attachmentImage.value
        val name = attachmentName?.let { "${UUID.randomUUID()}_${it}" }
        setAttachment(null, null)
        _chatObserverLoadingData.value = LoadingData.Loading()
        sendChatMessageUseCase(
            NewMessageItem(orderId, myId, toId, message, name, attachment),
            onSuccess = { _chatObserverLoadingData.value = LoadingData.Success(Unit) },
            onError = { _chatObserverLoadingData.value = LoadingData.Error(it) }
        )
    }

    fun setAttachment(uri: Uri?, attachment: Bitmap?) {
        _attachmentImage.value = attachment
        attachmentName = uri?.lastPathSegment
    }
}
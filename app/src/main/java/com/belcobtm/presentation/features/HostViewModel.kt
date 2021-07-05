package com.belcobtm.presentation.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.data.websockets.manager.WebSocketManager
import kotlinx.coroutines.launch

class HostViewModel(
    private val webSocketManager: WebSocketManager
): ViewModel() {

    fun disconnectFromSocket() {
        viewModelScope.launch {
            webSocketManager.disconnect()
        }
    }
}
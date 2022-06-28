package com.belcobtm.data.websockets.services

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.websockets.base.model.SocketState
import com.belcobtm.data.websockets.base.model.StompSocketRequest
import com.belcobtm.data.websockets.manager.SocketManager
import com.belcobtm.data.websockets.manager.WebSocketManager
import com.belcobtm.data.websockets.services.model.ServicesInfoResponse
import com.belcobtm.domain.mapSuspend
import com.belcobtm.domain.service.ServiceRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.lang.reflect.Type

class WebSocketServicesObserver(
    private val socketManager: WebSocketManager,
    private val serviceRepository: ServiceRepository,
    private val moshi: Moshi,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : ServicesObserver {

    private companion object {

        const val DESTINATION_VALUE = "/user/queue/service"
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var subscribeJob: Job? = null

    override fun connect() {
        subscribeJob = ioScope.launch {
            socketManager.observeSocketState()
                .filterIsInstance<SocketState.Connected>()
                .flatMapLatest {
                    val request = StompSocketRequest(
                        StompSocketRequest.SUBSCRIBE, mapOf(
                            SocketManager.ID_HEADER to sharedPreferencesHelper.userPhone,
                            SocketManager.DESTINATION_HEADER to DESTINATION_VALUE
                        )
                    )
                    socketManager.subscribe(DESTINATION_VALUE, request)
                }.filterNotNull()
                .collectLatest { response ->
                    response.mapSuspend { response ->
                        val responseType: Type = Types.newParameterizedType(
                            List::class.java,
                            ServicesInfoResponse::class.java
                        )
                        moshi.adapter<List<ServicesInfoResponse>>(responseType)
                            .fromJson(response.body)
                            ?.let { services ->
                                serviceRepository.updateServices(services)
                            }
                    }
                }
        }
    }

}

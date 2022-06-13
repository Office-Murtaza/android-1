package com.belcobtm.domain.support

class SupportChatInteractor(
    private val supportChatHelper: SupportChatHelper
) {

    fun init() = supportChatHelper.init()

}

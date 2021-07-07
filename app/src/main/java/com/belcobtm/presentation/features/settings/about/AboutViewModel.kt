package com.belcobtm.presentation.features.settings.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.BuildConfig
import com.belcobtm.domain.tools.IntentActions
import com.belcobtm.presentation.core.Const

class AboutViewModel(private val intentActions: IntentActions) : ViewModel() {

    private val _appVersion = MutableLiveData<String>()
    val appVersion: LiveData<String> = _appVersion

    init {
        populateAppVersion()
    }

    fun handleItemClick(item: AboutItem) = when (item) {
        AboutItem.TERMS -> intentActions.openViewActivity(Const.TERMS_URL)
        AboutItem.PRIVACY -> intentActions.openViewActivity(Const.PRIVACY_URL)
        AboutItem.COMPLAINT -> intentActions.openViewActivity(Const.COMPLAINT_URL)
    }

    private fun populateAppVersion() {
        _appVersion.value = BuildConfig.VERSION_NAME
    }
}

enum class AboutItem {
    TERMS,
    PRIVACY,
    COMPLAINT
}

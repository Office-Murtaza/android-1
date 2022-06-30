package com.belcobtm.presentation.screens.settings.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.BuildConfig
import com.belcobtm.R
import com.belcobtm.presentation.core.Const
import com.belcobtm.presentation.core.provider.string.StringProvider

class AboutViewModel(private val stringProvider: StringProvider) : ViewModel() {

    private val _appVersion = MutableLiveData<String>()
    val appVersion: LiveData<String> = _appVersion

    private val _link = MutableLiveData<Pair<String, String>>()
    val link: LiveData<Pair<String, String>>
        get() = _link

    init {
        populateAppVersion()
    }

    fun handleItemClick(item: AboutItem) {
        _link.value = when (item) {
            AboutItem.TERMS ->
                stringProvider.getString(R.string.about_screen_terms_picker_title) to Const.TERMS_URL
            AboutItem.PRIVACY ->
                stringProvider.getString(R.string.about_screen_privacy_picker_title) to Const.PRIVACY_URL
            AboutItem.COMPLAINT ->
                stringProvider.getString(R.string.about_screen_complaint_picker_title) to Const.COMPLAINT_URL
        }
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

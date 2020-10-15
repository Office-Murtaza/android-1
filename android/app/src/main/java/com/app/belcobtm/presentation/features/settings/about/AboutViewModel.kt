package com.app.belcobtm.presentation.features.settings.about

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.belcobtm.App
import com.app.belcobtm.domain.tools.IntentActions
import com.app.belcobtm.presentation.core.Const

class AboutViewModel(
    application: Application,
    private val intentActions: IntentActions
) : AndroidViewModel(application) {

    private val _appVersion = MutableLiveData<String>()
    val appVersion: LiveData<String> = _appVersion

    init {
        populateAppVersion()
    }

    fun handleItemClick(item: AboutItem) = when (item) {
        AboutItem.TERMS -> intentActions.openViewActivity(Const.TERMS_URL)
        AboutItem.SUPPORT -> intentActions.openViewActivity(Const.SUPPORT_URL)
    }

    private fun populateAppVersion() {
        val application = getApplication<App>()
        val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
        val versionName = packageInfo.versionName
        _appVersion.value = versionName
    }
}

enum class AboutItem {
    TERMS,
    SUPPORT
}

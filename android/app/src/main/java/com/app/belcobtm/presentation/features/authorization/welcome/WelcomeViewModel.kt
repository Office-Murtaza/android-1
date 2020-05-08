package com.app.belcobtm.presentation.features.authorization.welcome

import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.ClearAppDataUseCase

class WelcomeViewModel(private val clearAppDataUseCase: ClearAppDataUseCase) : ViewModel() {

    fun clearAppData() {
        clearAppDataUseCase.invoke{}
    }
}
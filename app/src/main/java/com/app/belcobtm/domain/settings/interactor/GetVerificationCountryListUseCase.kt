package com.app.belcobtm.domain.settings.interactor

import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.settings.item.VerificationCountryDataItem

class GetVerificationCountryListUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): List<VerificationCountryDataItem> = repository.getVerificationCountries()
}
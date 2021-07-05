package com.belcobtm.domain.settings.interactor

import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.settings.item.VerificationCountryDataItem

class GetVerificationCountryListUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): List<VerificationCountryDataItem> = repository.getVerificationCountries()
}
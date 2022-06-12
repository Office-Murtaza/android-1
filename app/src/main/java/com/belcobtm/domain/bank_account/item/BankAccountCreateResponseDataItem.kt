package com.belcobtm.domain.bank_account.item

data class BankAccountCreateResponseDataItem(
    val bankAccount: BankAccountDataItem? = null,
    val validationError: BankAccountValidationErrorDataItem? = null,
)

class BankAccountValidationErrorDataItem {
    var accountNumberValidationError: String? = null
    var routingNumberValidationError: String? = null
    var ibanValidationError: String? = null
    var nameValidationError: String? = null
    var countryValidationError: String? = null
    var provinceValidationError: String? = null
    var cityValidationError: String? = null
    var addressValidationError: String? = null
    var zipCodeValidationError: String? = null
    var bankNameValidationError: String? = null
    var bankCountryValidationError: String? = null
    var bankCityValidationError: String? = null

}
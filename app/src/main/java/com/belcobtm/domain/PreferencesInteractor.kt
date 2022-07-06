package com.belcobtm.domain

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper

class PreferencesInteractor(
    private val prefHelper: SharedPreferencesHelper
) {

    val apiSeed: String
        get() = prefHelper.apiSeed

    val firebaseToken: String
        get() = prefHelper.firebaseToken

    val userId: String
        get() = prefHelper.userId

    var userPhone: String
        set(value) {
            prefHelper.userPhone = value
        }
        get() = prefHelper.userPhone

    val userFullName: String
        get() = "${prefHelper.userFirstName} ${prefHelper.userLastName}"

    var userStatus: String
        set(value) {
            prefHelper.userStatus = value
        }
        get() = prefHelper.userStatus

}

package com.app.belcobtm.presentation.features.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedFragment

class SecurityViewModel : ViewModel() {

    private val _actionData = SingleLiveData<SecurityAction>()
    val actionData: LiveData<SecurityAction> = _actionData

    fun handleItemClick(securityItem: SecurityItem) {
        val direction = when (securityItem) {
            SecurityItem.PHONE -> {
                SecurityFragmentDirections.toDisplayPhone()
            }
            SecurityItem.PASS -> {
                SecurityFragmentDirections.toUpdatePassword()
            }
            SecurityItem.PIN -> {
                SecurityFragmentDirections.toPinCode()
            }
            SecurityItem.SEED -> {
                SecurityFragmentDirections.toPassword(
                    R.id.password_to_create_seed_fragment,
                    R.string.seed_phrase_label,
                    CreateSeedFragment.MODE_SETTINGS
                )
            }
            SecurityItem.UNLINK -> {
                SecurityFragmentDirections.toUnlink()
            }
        }
        _actionData.value = SecurityAction.NavigateAction(direction)
    }
}

enum class SecurityItem {
    PHONE,
    PASS,
    PIN,
    SEED,
    UNLINK
}

sealed class SecurityAction {
    data class NavigateAction(val navDirections: NavDirections) : SecurityAction()
}
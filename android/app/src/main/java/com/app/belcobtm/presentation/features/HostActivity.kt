package com.app.belcobtm.presentation.features

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import com.app.belcobtm.R
import com.app.belcobtm.domain.authorization.AuthorizationStatus
import com.app.belcobtm.domain.authorization.interactor.AuthorizationStatusGetUseCase
import com.app.belcobtm.domain.authorization.interactor.ClearAppDataUseCase
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment
import org.koin.android.ext.android.inject

class HostActivity : AppCompatActivity() {
    private val authorizationStatusUseCase: AuthorizationStatusGetUseCase by inject()
    private val clearAppDataUseCase: ClearAppDataUseCase by inject()
    private lateinit var currentNavFragment: NavHostFragment
    private val authorizationBroadcast: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) = if (intent.getBooleanExtra(KEY_IS_USER_UNAUTHORIZED, false)) {
            showAuthorizationScreen()
        } else {
            showPinScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        when (authorizationStatusUseCase.invoke()) {
            AuthorizationStatus.PIN_CODE_CREATE -> showPinScreen(PinCodeFragment.KEY_PIN_MODE_CREATE)
            AuthorizationStatus.PIN_CODE_ENTER -> showPinScreen()
            else -> showAuthorizationScreen()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return false
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(TAG_USER_AUTHORIZATION)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(authorizationBroadcast, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(authorizationBroadcast)
    }

    fun showMainScreen() = setHostFragment(HostNavigationFragment())

    fun showPinScreen(mode: String = PinCodeFragment.KEY_PIN_MODE_ENTER) {
        setHostFragment(
            NavHostFragment.create(
                R.navigation.nav_pin_code,
                bundleOf(PinCodeFragment.TAG_PIN_MODE to mode)
            )
        )
    }

    fun showAuthorizationScreen() {
        clearAppDataUseCase.invoke()
        setHostFragment(NavHostFragment.create(R.navigation.nav_authorization))
    }

    private fun setHostFragment(fragment: NavHostFragment) {
        currentNavFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment, fragment.javaClass.name)
            .commit()
    }

    companion object {
        private const val TAG_USER_AUTHORIZATION = "tag_broadcast_user_unauthorized"
        private const val KEY_IS_USER_UNAUTHORIZED = "key_is_user_unauthorized"
    }
}

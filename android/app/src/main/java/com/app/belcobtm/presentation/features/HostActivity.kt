package com.app.belcobtm.presentation.features

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.app.belcobtm.R
import com.app.belcobtm.domain.authorization.AuthorizationStatus
import com.app.belcobtm.domain.authorization.interactor.AuthorizationStatusGetUseCase
import com.app.belcobtm.presentation.features.authorization.welcome.WelcomeFragment
import org.koin.android.ext.android.inject

class HostActivity : AppCompatActivity() {
    private val authorizationStatusUseCase: AuthorizationStatusGetUseCase by inject()
    private lateinit var currentNavFragment: NavHostFragment
//    private val authorizationBroadcast: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) = showAuthorizationScreen()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        when (val status = authorizationStatusUseCase.invoke()) {
            AuthorizationStatus.AUTHORIZED -> showMainScreen()
            AuthorizationStatus.UNAUTHORIZED,
            AuthorizationStatus.SEED_PHRASE_CREATE,
            AuthorizationStatus.SEED_PHRASE_ENTER,
            AuthorizationStatus.PIN_CODE_CREATE,
            AuthorizationStatus.PIN_CODE_ENTER -> showAuthorizationScreen(status)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return false
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(TAG_USER_UNAUTHORIZED)
//        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(authorizationBroadcast, intentFilter)
    }

    fun showMainScreen() = setHostFragment(HostNavigationFragment())

    fun showAuthorizationScreen(authorizationStatus: AuthorizationStatus? = null) {
        val navFragment = if (authorizationStatus != null) {
            NavHostFragment.create(
                R.navigation.nav_authorization,
                bundleOf(WelcomeFragment.TAG_AUTHORIZATION_STATUS to authorizationStatus.ordinal)
            )
        } else {
            NavHostFragment.create(R.navigation.nav_authorization)
        }
        setHostFragment(navFragment)
    }

    private fun setHostFragment(fragment: NavHostFragment) {
        currentNavFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment, fragment.javaClass.name)
            .commit()
    }

    companion object {
        const val TAG_USER_UNAUTHORIZED = "tag_broadcast_user_unauthorized"
    }
}

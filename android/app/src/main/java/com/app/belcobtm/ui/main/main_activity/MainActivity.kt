package com.app.belcobtm.ui.main.main_activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.auth.pin.PinActivity
import com.app.belcobtm.ui.auth.welcome.WelcomeActivity
import com.app.belcobtm.ui.main.atm.AtmFragment
import com.app.belcobtm.ui.main.coins.balance.BalanceFragment
import com.app.belcobtm.ui.main.settings.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseMvpActivity<MainContract.View, MainContract.Presenter>(),
    MainContract.View
    , BottomNavigationView.OnNavigationItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPresenter.checkPinEntered()

        bottom_bar.setOnNavigationItemSelectedListener(this)

        setFragment(BalanceFragment())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (bottom_bar.selectedItemId != item.itemId) {
            when (item.itemId) {
                R.id.menu_wallet -> {
                    setFragment(BalanceFragment())
                    return true
                }
                R.id.menu_atm -> {
                    setFragment(AtmFragment())
                    return true
                }
                R.id.menu_settings -> {
                    setFragment(SettingsFragment())
                    return true
                }
            }
        }
        return false
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    override fun onTokenNotSaved() {
        finishAffinity()
        startActivity(Intent(this, WelcomeActivity::class.java))
    }

    override fun onPinSaved() {
        val mode = PinActivity.Companion.Mode.MODE_PIN
        val intent = PinActivity.getIntent(this, mode)
        startActivityForResult(intent, mode.ordinal)
    }

    override fun onPinNotSaved() {
        val mode = PinActivity.Companion.Mode.MODE_CREATE_PIN
        val intent = PinActivity.getIntent(this, mode)
        startActivityForResult(intent, mode.ordinal)
    }
}

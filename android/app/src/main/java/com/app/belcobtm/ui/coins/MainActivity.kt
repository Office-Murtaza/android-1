package com.app.belcobtm.ui.coins

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.auth.welcome.WelcomeActivity
import com.app.belcobtm.ui.coins.atm.AtmFragment
import com.app.belcobtm.ui.coins.balance.BalanceFragment
import com.app.belcobtm.util.pref
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
    , BottomNavigationView.OnNavigationItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_bar.setOnNavigationItemSelectedListener(this)


        setFragment(BalanceFragment())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
           R.id.menu_wallet -> {
                setFragment(BalanceFragment())
                return true
            }
            R.id.menu_atm -> {
                setFragment(AtmFragment())//todo atm fragment
                return true
            }
            R.id.menu_settings -> {
                setFragment(BalanceFragment())//todo settings fragment
                return true
            }
        }
        return false
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}

package com.app.belcobtm.presentation.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentNavigationBinding
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
import com.google.android.material.bottomnavigation.BottomNavigationView


class HostNavigationFragment : NavHostFragment(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val ARG_NAV_ID = "HostNavigationFragment.arg.nav.id"
        private const val NAV_ID_UNKKNOWN = -1
    }

    private var currentNavigationGraphId: Int = NAV_ID_UNKKNOWN
    private lateinit var navHostController: NavController
    private lateinit var binding: FragmentNavigationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNavigationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostController = navHostFragment.navController

        currentNavigationGraphId = savedInstanceState
            ?.getInt(ARG_NAV_ID, NAV_ID_UNKKNOWN) ?: NAV_ID_UNKKNOWN
        if (currentNavigationGraphId == NAV_ID_UNKKNOWN) {
            currentNavigationGraphId = R.navigation.nav_wallet
            navHostController.setGraph(R.navigation.nav_wallet)
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_NAV_ID, currentNavigationGraphId)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return false
    }

    fun showBottomMenu() = binding.bottomNavigationView.show()

    fun hideBottomMenu() = binding.bottomNavigationView.hide()

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val nextGraph = when (item.itemId) {
            R.id.nav_wallet -> R.navigation.nav_wallet
            R.id.nav_deals -> R.navigation.nav_deals
            R.id.nav_locations -> R.navigation.nav_atm
            R.id.nav_settings -> R.navigation.nav_settings
            else -> throw IllegalArgumentException()
        }
        if (currentNavigationGraphId != nextGraph) {
            currentNavigationGraphId = nextGraph
            navHostController.setGraph(nextGraph)
            return true
        }
        return false
    }
}

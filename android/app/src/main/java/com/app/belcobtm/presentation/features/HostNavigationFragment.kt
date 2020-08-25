package com.app.belcobtm.presentation.features

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.setupWithNavController
import com.app.belcobtm.presentation.core.extensions.show
import kotlinx.android.synthetic.main.fragment_navigation.*
import permissions.dispatcher.NeedsPermission

class HostNavigationFragment : NavHostFragment() {
    private lateinit var currentNavHostController: LiveData<NavController>
    private val navGraphIds = listOf(
        R.navigation.nav_wallet,
        R.navigation.nav_atm,
        R.navigation.nav_settings
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_navigation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentNavHostController = bottomNavigationView.setupWithNavController(
            navGraphIds,
            childFragmentManager,
            R.id.navHostFragment,
            Intent()
        )
    }

    fun getCurrentNavHostController(): NavController? = currentNavHostController.value

    @NeedsPermission(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun checkPermissions() = Unit

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return false
    }

    fun showBottomMenu() = bottomNavigationView.show()

    fun hideBottomMenu() = bottomNavigationView.hide()
}
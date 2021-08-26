package com.belcobtm.presentation.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.belcobtm.R
import com.belcobtm.databinding.FragmentNavigationBinding
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainFragment : BaseFragment<FragmentNavigationBinding>() {

    companion object {
        const val INNER_DESTINATION_ID = "inner.destination.id"
        const val INNER_DESTINATION_BUNDLE_ID = "inner.destination.bundle.id"
    }

    override val isToolbarEnabled: Boolean
        get() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNavigationBinding =
        FragmentNavigationBinding.inflate(inflater, container, false)

}

package com.belcobtm.presentation.features

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.belcobtm.R
import com.belcobtm.databinding.FragmentNavigationBinding
import com.belcobtm.presentation.core.ui.fragment.BaseFragment


class MainFragment : BaseFragment<FragmentNavigationBinding>() {

    companion object {
        const val KEY_DEEPLINK = "key_deeplink"
    }

    override val isToolbarEnabled: Boolean
        get() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        binding.bottomNavigationView.setupWithNavController(navHostFragment.navController)
        arguments?.getString(KEY_DEEPLINK)?.let { deeplink ->
            navHostFragment.navController.navigate(Uri.parse(deeplink))
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNavigationBinding =
        FragmentNavigationBinding.inflate(inflater, container, false)

}

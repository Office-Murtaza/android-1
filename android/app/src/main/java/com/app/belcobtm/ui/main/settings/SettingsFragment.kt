package com.app.belcobtm.ui.main.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.belcobtm.R
import com.app.belcobtm.ui.main.settings.check_pass.CheckPassActivity
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        settings_unlink.setOnClickListener { startActivity(Intent(activity, UnlinkActivity::class.java))}
        settings_seed.setOnClickListener { CheckPassActivity.startActivity(context, CheckPassActivity.Companion.Mode.MODE_SEED) }

    }
}

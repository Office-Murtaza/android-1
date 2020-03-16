package com.app.belcobtm.ui.main.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.belcobtm.R
import com.app.belcobtm.ui.main.settings.change_pass.ChangePassActivity
import com.app.belcobtm.ui.main.settings.check_pass.CheckPassActivity
import com.app.belcobtm.ui.main.settings.phone.ShowPhoneActivity
import com.app.belcobtm.presentation.features.settings.verification.info.VerificationInfoActivity
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        settings_phone.setOnClickListener {
            startActivity(Intent(activity, ShowPhoneActivity::class.java))
        }
        settings_change_pass.setOnClickListener {
            ChangePassActivity.start(activity, ChangePassActivity.Companion.Mode.MODE_CHANGE_PASS)
        }
        settings_change_pin.setOnClickListener {
            ChangePassActivity.start(activity, ChangePassActivity.Companion.Mode.MODE_CHANGE_PIN)
        }
        settings_seed.setOnClickListener {
            CheckPassActivity.start(context, CheckPassActivity.Companion.Mode.MODE_OPEN_SEED)
        }
        settings_unlink.setOnClickListener {
            startActivity(Intent(activity, UnlinkActivity::class.java))
        }
        verificationButtonView.setOnClickListener {
            startActivity(Intent(activity, VerificationInfoActivity::class.java))
        }
    }
}

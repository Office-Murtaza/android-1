package com.belcobtm.domain.tools

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.openViewActivity(path: String, title: String = "") {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
    Intent.createChooser(intent, title)
    startActivity(Intent.createChooser(intent, title))
}
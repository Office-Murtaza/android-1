package com.belcobtm.presentation.core.provider.string

import android.content.res.Resources

class ResourceStringProvider(
    private val resources: Resources
) : StringProvider {

    override fun getString(res: Int): String =
        resources.getString(res)

    override fun getString(res: Int, vararg args: Any?): String =
        resources.getString(res, *args)
}
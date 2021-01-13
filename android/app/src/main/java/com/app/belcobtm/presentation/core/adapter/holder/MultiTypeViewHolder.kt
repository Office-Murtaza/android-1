package com.app.belcobtm.presentation.core.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.presentation.core.adapter.model.ListItem

abstract class MultiTypeViewHolder<M : ListItem>(root: View) : RecyclerView.ViewHolder(root) {

    protected lateinit var model: M

    abstract fun bind(model: M)

    open fun bindPayload(model: M, payloads: List<Any>) {
        this.model = model
        bind(model)
    }

    fun onBind(model: M) {
        this.model = model
        bind(model)
    }
}
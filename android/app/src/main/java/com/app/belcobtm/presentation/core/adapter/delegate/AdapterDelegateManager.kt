package com.app.belcobtm.presentation.core.adapter.delegate

import android.util.SparseArray
import android.view.ViewGroup
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.core.adapter.model.ListItem

class AdapterDelegateManager {

    private val delegates = SparseArray<AdapterDelegate<*, *>>()

    fun addDelegate(delegate: AdapterDelegate<*, *>) {
        delegates.get(delegate.viewType)?.let {
            throw IllegalArgumentException("Delegate with viewType ${delegate.viewType} already registered $it")
        }
        delegates.append(delegate.viewType, delegate)
    }

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiTypeViewHolder<*> =
        delegates[viewType]?.createHolder(parent) ?: notSupportedViewTypeError(viewType)

    fun onBindViewHolder(holder: MultiTypeViewHolder<*>, content: List<ListItem>, position: Int) {
        (holder as MultiTypeViewHolder<ListItem>).onBind(content[position])
    }

    fun onBindViewHolder(
        holder: MultiTypeViewHolder<*>,
        content: List<ListItem>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        (holder as MultiTypeViewHolder<ListItem>).bindPayload(content[position], payloads)
    }

    private fun notSupportedViewTypeError(viewType: Int): Nothing =
        throw IllegalStateException("Cannot create viewholder. Unknown viewType $viewType")
}
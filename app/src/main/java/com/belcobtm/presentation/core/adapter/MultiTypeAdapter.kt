package com.belcobtm.presentation.core.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegateManager
import com.belcobtm.presentation.core.adapter.diffutl.ListItemDiffUtil
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.core.adapter.model.ListItem

open class MultiTypeAdapter(
    private val diffUtil: ListItemDiffUtil = ListItemDiffUtil()
) : RecyclerView.Adapter<MultiTypeViewHolder<*>>() {

    var content: MutableList<ListItem> = ArrayList()
        private set
    private val manager = AdapterDelegateManager()

    fun registerDelegate(adapterDelegate: AdapterDelegate<*, *>) {
        manager.addDelegate(adapterDelegate)
    }

    fun update(items: List<ListItem>) {
        diffUtil.oldList = content.toList()
        diffUtil.newList = items
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        content.clear()
        content.addAll(items)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiTypeViewHolder<*> =
        manager.onCreateViewHolder(parent, viewType)

    override fun getItemCount(): Int = content.size

    override fun onBindViewHolder(holder: MultiTypeViewHolder<*>, position: Int) {
        manager.onBindViewHolder(holder, content, position)
    }

    override fun onBindViewHolder(holder: MultiTypeViewHolder<*>, position: Int, payloads: MutableList<Any>) {
        manager.onBindViewHolder(holder, content, position, payloads)
    }

    override fun getItemViewType(position: Int): Int =
        content[position].type
}
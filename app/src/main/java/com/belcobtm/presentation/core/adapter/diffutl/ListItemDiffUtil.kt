package com.belcobtm.presentation.core.adapter.diffutl

import androidx.recyclerview.widget.DiffUtil
import com.belcobtm.presentation.core.adapter.model.ListItem

open class ListItemDiffUtil : DiffUtil.Callback() {

    var oldList: List<ListItem> = emptyList()
    var newList: List<ListItem> = emptyList()

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]
}
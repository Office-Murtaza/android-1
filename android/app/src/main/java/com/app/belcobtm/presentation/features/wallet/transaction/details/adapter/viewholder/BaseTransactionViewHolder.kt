package com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.presentation.features.wallet.transaction.details.adapter.TransactionDetailsAdapter

abstract class BaseTransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bindHodler(item: TransactionDetailsAdapter.Item)
}

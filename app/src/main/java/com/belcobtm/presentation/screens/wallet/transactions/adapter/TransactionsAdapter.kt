package com.belcobtm.presentation.screens.wallet.transactions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.belcobtm.R
import com.belcobtm.databinding.ItemTransactionBinding
import com.belcobtm.domain.transaction.type.TransactionStatusType
import com.belcobtm.presentation.tools.extensions.getResText
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.screens.wallet.transactions.item.TransactionsAdapterItem
import com.belcobtm.presentation.screens.wallet.transactions.item.TransactionsAdapterItemCallback

class TransactionsAdapter(
    private val itemClickListener: (item: TransactionsAdapterItem) -> Unit
) : ListAdapter<TransactionsAdapterItem, TransactionsAdapter.Holder>(TransactionsAdapterItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = Holder(binding)
        binding.root.setOnClickListener { itemClickListener.invoke(getItem(holder.adapterPosition)) }
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val context = holder.itemView.context
        val item = getItem(position)
        with(holder.binding) {
            dateView.text = item.date
            typeView.text = context.getString(item.type.getResText())
            amountView.text = item.cryptoAmount.toStringCoin()
            updateStatusView(statusView, item.status)
        }
    }

    private fun updateStatusView(textView: AppCompatTextView, status: TransactionStatusType) {
        val resText: Int
        val resBackground: Int
        val resTextColor: Int

        when (status) {
            TransactionStatusType.PENDING -> {
                resText = R.string.transaction_status_pending
                resBackground = R.drawable.bg_status_pending
                resTextColor = R.color.colorStatusPending
            }
            TransactionStatusType.COMPLETE -> {
                resTextColor = R.color.mainGreen
                resBackground = R.drawable.bg_status_complete
                resText = R.string.transaction_status_complete
            }
            TransactionStatusType.FAIL -> {
                resText = R.string.transaction_status_fail
                resBackground = R.drawable.bg_status_fail
                resTextColor = R.color.mainRed
            }
            else -> {
                resText = R.string.transaction_status_unknown
                resBackground = R.drawable.bg_status_unknown
                resTextColor = R.color.colorStatusUnknown
            }
        }

        textView.setTextColor(ContextCompat.getColor(textView.context, resTextColor))
        textView.setBackgroundDrawable(ContextCompat.getDrawable(textView.context, resBackground))
        textView.setText(resText)
    }

    class Holder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)

}

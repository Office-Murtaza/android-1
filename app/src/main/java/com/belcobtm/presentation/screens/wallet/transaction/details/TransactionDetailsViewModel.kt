package com.belcobtm.presentation.screens.wallet.transaction.details

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.interactor.ObserveTransactionDetailsUseCase
import com.belcobtm.domain.transaction.item.TransactionDomainModel
import com.belcobtm.domain.transaction.type.TransactionCashStatusType
import com.belcobtm.domain.transaction.type.TransactionStatusType
import com.belcobtm.domain.transaction.type.TransactionType
import com.belcobtm.presentation.core.DateFormat
import com.belcobtm.presentation.core.QRUtils
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.screens.wallet.transaction.details.adapter.TransactionDetailsAdapter
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.extensions.toStringPercents
import com.belcobtm.presentation.tools.formatter.Formatter
import com.belcobtm.presentation.tools.formatter.PhoneNumberFormatter
import kotlinx.coroutines.flow.map

class TransactionDetailsViewModel(
    private val transactionId: String,
    private val coinCode: String,
    private val phoneFormatter: PhoneNumberFormatter,
    private val transactionDetailsUseCase: ObserveTransactionDetailsUseCase,
    private val currencyFormatter: Formatter<Double>,
    private val stringProvider: StringProvider,
) : ViewModel() {

    val transactionDetailsLiveData: LiveData<LoadingData<List<TransactionDetailsAdapter.Item>>>
        get() = transactionDetailsUseCase.invoke(
            ObserveTransactionDetailsUseCase.Params(
                transactionId,
                coinCode
            )
        ).map {
            if (it != null) {
                LoadingData.Success(mapToItemList(it))
            } else {
                LoadingData.Error(Failure.ServerError())
            }
        }.asLiveData(viewModelScope.coroutineContext)

    private fun mapToItemList(
        dataItem: TransactionDomainModel
    ): List<TransactionDetailsAdapter.Item> {
        val result = mutableListOf<TransactionDetailsAdapter.Item>()
        // id block
        if (dataItem.hash != null && dataItem.link != null) {
            val idBlockItem = TransactionDetailsAdapter.Item.Id(
                R.string.transaction_details_hash,
                dataItem.hash,
                dataItem.link
            )
            result.add(idBlockItem)
        }
        // transaction type block
        if (dataItem.type != TransactionType.UNKNOWN) {
            val transactionTypeBlockItem = TransactionDetailsAdapter.Item.Type(dataItem.type)
            result.add(transactionTypeBlockItem)
        }
        // transaction status block
        if (dataItem.statusType != TransactionStatusType.UNKNOWN) {
            val transactionStatusBlockItem = TransactionDetailsAdapter.Item.Status(
                R.string.transaction_details_status,
                getTextResFromTransactionStatus(dataItem.statusType),
                getImageResFromTransactionStatus(dataItem.statusType)
            )
            result.add(transactionStatusBlockItem)
        }
        // amount block
        if (dataItem.cryptoAmount != null) {
            val amountBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_amount,
                dataItem.cryptoAmount.toStringCoin().plus(" ").plus(coinCode)
            )
            result.add(amountBlockItem)
        }
        // fee block
        if (dataItem.cryptoFee != null) {
            val feeBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_fee,
                dataItem.cryptoFee.toStringCoin().plus(" ").plus(coinCode)
            )
            result.add(feeBlockItem)
        }
        // confirmation block
        if (dataItem.confiramtions != null) {
            val confirmationsBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_confirmations,
                dataItem.confiramtions.toString()
            )
            result.add(confirmationsBlockItem)
        }
        // from address block
        if (dataItem.fromAddress != null) {
            val fromAddressBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_from_address,
                dataItem.fromAddress
            )
            result.add(fromAddressBlockItem)
        }
        // to address block
        if (dataItem.toAddress != null) {
            val toAddressBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_to_address,
                dataItem.toAddress
            )
            result.add(toAddressBlockItem)
        }
        // from phone
        if (dataItem.fromPhone != null) {
            val fromPhoneBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_from_phone,
                phoneFormatter.format(dataItem.fromPhone)
            )
            result.add(fromPhoneBlockItem)
        }
        // to phone
        if (dataItem.toPhone != null) {
            val fromPhoneBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_to_phone,
                phoneFormatter.format(dataItem.toPhone)
            )
            result.add(fromPhoneBlockItem)
        }
        // gif
        if (dataItem.imageId != null) {
            val gifBlockItem = TransactionDetailsAdapter.Item.GIF(
                dataItem.imageId,
                dataItem.message.orEmpty()
            )
            result.add(gifBlockItem)
        }
        // swap id
        val isSwap =
            dataItem.type == TransactionType.SEND_SWAP || dataItem.type == TransactionType.RECEIVE_SWAP
        if (isSwap && dataItem.refTxId != null && dataItem.refLink != null) {
            val swapBlockItem = TransactionDetailsAdapter.Item.Id(
                R.string.transaction_details_swap_id,
                dataItem.refTxId,
                dataItem.refLink
            )
            result.add(swapBlockItem)
        }
        // swap amount
        if (isSwap && dataItem.refCoin != null && dataItem.refCryptoAmount != null) {
            val swapBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_swap_amount,
                dataItem.refCryptoAmount.toStringCoin().plus(" ").plus(dataItem.refCoin)
            )
            result.add(swapBlockItem)
        }
        // fiat amount
        if (dataItem.fiatAmount != null) {
            val sellBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_sell_amount,
                currencyFormatter.format(dataItem.fiatAmount)
            )
            result.add(sellBlockItem)
        }
        // fee percent
        if (dataItem.feePercent != null) {
            val feePercentBlockItem = TransactionDetailsAdapter.Item.Regular(
                R.string.transaction_details_fee_percent,
                stringProvider.getString(
                    R.string.transaction_details_fee_percent_value_format,
                    dataItem.feePercent.toStringPercents(),
                )
            )
            result.add(feePercentBlockItem)
        }
        // cash status
        if (dataItem.cashStatusType != TransactionCashStatusType.UNKNOWN) {
            val cashStatusBlockItem = TransactionDetailsAdapter.Item.Status(
                R.string.transaction_details_cash_status,
                getTextResFromTransactionCashStatus(dataItem.cashStatusType),
                getImageResFromTransactionCashStatus(dataItem.cashStatusType)
            )
            result.add(cashStatusBlockItem)
        }
        // qr
        if (dataItem.sellInfo != null) {
            val bitmap = QRUtils.getSpacelessQR(dataItem.sellInfo, 600, 600)
            if (bitmap != null) {
                val qrBlockItem = TransactionDetailsAdapter.Item.QR(bitmap)
                result.add(qrBlockItem)
            }
        }
        // date block
        val dateBlockItem = TransactionDetailsAdapter.Item.Regular(
            R.string.transaction_details_date,
            DateFormat.sdfLong.format(dataItem.timestamp)
        )
        result.add(dateBlockItem)
        // to prevent any modifications outside
        return result.toList()
    }

    @DrawableRes
    private fun getImageResFromTransactionStatus(status: TransactionStatusType): Int {
        return when (status) {
            TransactionStatusType.PENDING -> R.drawable.ic_blue_dots
            TransactionStatusType.COMPLETE -> R.drawable.ic_check_green
            else -> R.drawable.ic_red_cross
        }
    }

    @StringRes
    private fun getTextResFromTransactionStatus(status: TransactionStatusType): Int {
        return when (status) {
            TransactionStatusType.PENDING -> R.string.transaction_status_pending
            TransactionStatusType.COMPLETE -> R.string.transaction_status_complete
            TransactionStatusType.FAIL -> R.string.transaction_status_fail
            else -> R.string.base_screen_server_error_title

        }
    }

    @DrawableRes
    private fun getImageResFromTransactionCashStatus(status: TransactionCashStatusType): Int {
        return when (status) {
            TransactionCashStatusType.PENDING -> R.drawable.ic_dots
            TransactionCashStatusType.AVAILABLE -> R.drawable.ic_ring
            TransactionCashStatusType.WITHDRAWN -> R.drawable.ic_arrow_down
            else -> R.drawable.ic_red_cross
        }
    }

    @StringRes
    private fun getTextResFromTransactionCashStatus(status: TransactionCashStatusType): Int {
        return when (status) {
            TransactionCashStatusType.PENDING -> R.string.transition_details_screen_pending
            TransactionCashStatusType.AVAILABLE -> R.string.transition_details_screen_available
            TransactionCashStatusType.WITHDRAWN -> R.string.transition_details_screen_withdrawn
            else -> R.string.base_screen_server_error_title
        }
    }

}

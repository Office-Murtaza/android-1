package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.interactor.GetCoinFeeMapUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.features.authorization.pin.PinViewModel
import com.app.belcobtm.presentation.features.authorization.wallet.create.CreateWalletViewModel
import com.app.belcobtm.presentation.features.authorization.wallet.recover.RecoverWalletViewModel
import com.app.belcobtm.presentation.features.authorization.welcome.WelcomeViewModel
import com.app.belcobtm.presentation.features.settings.verification.blank.VerificationBlankViewModel
import com.app.belcobtm.presentation.features.settings.verification.info.VerificationInfoViewModel
import com.app.belcobtm.presentation.features.settings.verification.vip.VerificationVipViewModel
import com.app.belcobtm.presentation.features.wallet.add.AddWalletViewModel
import com.app.belcobtm.presentation.features.wallet.balance.BalanceViewModel
import com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin.ExchangeCoinToCoinViewModel
import com.app.belcobtm.presentation.features.wallet.trade.create.TradeCreateViewModel
import com.app.belcobtm.presentation.features.wallet.trade.details.TradeDetailsBuyViewModel
import com.app.belcobtm.presentation.features.wallet.trade.edit.TradeEditViewModel
import com.app.belcobtm.presentation.features.wallet.trade.main.TradeViewModel
import com.app.belcobtm.presentation.features.wallet.trade.recall.TradeRecallViewModel
import com.app.belcobtm.presentation.features.wallet.transactions.TransactionsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { WelcomeViewModel(get()) }
    viewModel { BalanceViewModel(get()) }
    viewModel { (coinCode: String) -> TransactionsViewModel(coinCode, get(), get(), get(), get()) }
    viewModel { RecoverWalletViewModel(get(), get()) }
    viewModel { CreateWalletViewModel(get(), get()) }
    viewModel { PinViewModel(get(), get(), get()) }
    viewModel { VerificationInfoViewModel(get()) }
    viewModel { VerificationBlankViewModel(get(), get()) }
    viewModel { VerificationVipViewModel(get()) }
    viewModel { (coinItem: CoinDataItem, coinItemArrayList: ArrayList<CoinDataItem>) ->
        val feeMap = (get() as GetCoinFeeMapUseCase).getCoinFeeMap()[coinItem.code]
        ExchangeCoinToCoinViewModel(feeMap!!, coinItem, coinItemArrayList, get(), get())
    }
    viewModel { AddWalletViewModel(get(), get()) }
    viewModel { (latitude: Double, longitude: Double, intentCoinItem: CoinDataItem) ->
        TradeViewModel(latitude, longitude, intentCoinItem, get(), get(), get(), get())
    }
    viewModel { (coinItem: CoinDataItem) -> TradeDetailsBuyViewModel(coinItem, get()) }
    viewModel { (coinItem: CoinDataItem) -> TradeCreateViewModel(coinItem, get(), get()) }
    viewModel { TradeEditViewModel() }
    viewModel { (coinCode: String) ->
        TradeRecallViewModel(
            get<WalletRepository>().getCoinItemByCode(coinCode),
            get<WalletRepository>().getCoinFeeItemByCode(coinCode)
        )
    }
}
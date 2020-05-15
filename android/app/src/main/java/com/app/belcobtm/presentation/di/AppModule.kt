package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.wallet.interactor.GetCoinFeeMapUseCase
import com.app.belcobtm.presentation.features.authorization.pin.PinViewModel
import com.app.belcobtm.presentation.features.authorization.wallet.create.CreateWalletViewModel
import com.app.belcobtm.presentation.features.authorization.wallet.recover.RecoverWalletViewModel
import com.app.belcobtm.presentation.features.authorization.welcome.WelcomeViewModel
import com.app.belcobtm.presentation.features.settings.verification.blank.VerificationBlankViewModel
import com.app.belcobtm.presentation.features.settings.verification.info.VerificationInfoViewModel
import com.app.belcobtm.presentation.features.settings.verification.vip.VerificationVipViewModel
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import com.app.belcobtm.presentation.features.wallet.add.AddWalletViewModel
import com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin.ExchangeCoinToCoinViewModel
import com.app.belcobtm.presentation.features.wallet.trade.TradeViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { WelcomeViewModel(get()) }
    viewModel { RecoverWalletViewModel(get(), get()) }
    viewModel { CreateWalletViewModel(get(), get()) }
    viewModel { PinViewModel(get(), get(), get()) }
    viewModel { VerificationInfoViewModel(get()) }
    viewModel { VerificationBlankViewModel(get(), get()) }
    viewModel { VerificationVipViewModel(get()) }
    viewModel { (intentCoinItem: IntentCoinItem, intentCoinItemArrayList: ArrayList<IntentCoinItem>) ->
        val feeMap = (get() as GetCoinFeeMapUseCase).getCoinFeeMap()[intentCoinItem.coinCode]
        ExchangeCoinToCoinViewModel(feeMap!!, intentCoinItem, intentCoinItemArrayList, get(), get())
    }
    viewModel { AddWalletViewModel(get(), get()) }
    viewModel { (intentCoinItem: IntentCoinItem) ->
        TradeViewModel(intentCoinItem, get())
    }
}
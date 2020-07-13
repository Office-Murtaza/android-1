package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.wallet.LocalCoinType
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
import com.app.belcobtm.presentation.features.sms.code.SmsCodeViewModel
import com.app.belcobtm.presentation.features.wallet.add.AddWalletViewModel
import com.app.belcobtm.presentation.features.wallet.balance.BalanceViewModel
import com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin.ExchangeCoinToCoinViewModel
import com.app.belcobtm.presentation.features.wallet.staking.StakingViewModel
import com.app.belcobtm.presentation.features.wallet.trade.create.TradeCreateViewModel
import com.app.belcobtm.presentation.features.wallet.trade.details.TradeDetailsBuyViewModel
import com.app.belcobtm.presentation.features.wallet.trade.edit.TradeEditViewModel
import com.app.belcobtm.presentation.features.wallet.trade.main.TradeViewModel
import com.app.belcobtm.presentation.features.wallet.trade.recall.TradeRecallViewModel
import com.app.belcobtm.presentation.features.wallet.trade.reserve.TradeReserveViewModel
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
        val feeMap = (get() as GetCoinFeeMapUseCase).getCoinFeeMap()
        val fromCoinFee = feeMap[coinItem.code]
        ExchangeCoinToCoinViewModel(
            fromCoinFee!!,
            coinItem,
            coinItemArrayList,
            feeMap,
            get(),
            get()
        )
    }
    viewModel { AddWalletViewModel(get(), get()) }
    viewModel { (latitude: Double, longitude: Double, coinCode: String) ->
        TradeViewModel(coinCode, latitude, longitude, get(), get(), get(), get(), get())
    }
    viewModel { (coinCode: String) ->
        val coinDataItem = get<WalletRepository>().getCoinItemByCode(coinCode)
        TradeDetailsBuyViewModel(coinDataItem, get())
    }
    viewModel { (coinCode: String) ->
        TradeCreateViewModel(get<WalletRepository>().getCoinItemByCode(coinCode), get(), get())
    }
    viewModel { TradeEditViewModel() }
    viewModel { (coinCode: String) ->
        TradeRecallViewModel(
            get<WalletRepository>().getCoinItemByCode(coinCode),
            get<WalletRepository>().getCoinFeeItemByCode(coinCode),
            get(),
            get()
        )
    }
    viewModel { (coinCode: String) ->
        TradeReserveViewModel(
            get<WalletRepository>().getCoinItemByCode(coinCode),
            get<WalletRepository>().getCoinFeeItemByCode(coinCode),
            get(),
            get()
        )
    }
    viewModel {
        val coinDataItem = get<WalletRepository>().getCoinItemByCode(LocalCoinType.CATM.name)
        val coinFee = get<WalletRepository>().getCoinFeeItemByCode(LocalCoinType.CATM.name)
        StakingViewModel(coinDataItem, coinFee, get(), get(), get(), get(), get(), get())
    }
    viewModel { (phone: String) ->
        SmsCodeViewModel(phone, get())
    }
}
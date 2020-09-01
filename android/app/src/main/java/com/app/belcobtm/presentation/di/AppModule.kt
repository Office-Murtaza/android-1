package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.presentation.features.atm.AtmViewModel
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedViewModel
import com.app.belcobtm.presentation.features.authorization.create.wallet.CreateWalletViewModel
import com.app.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedViewModel
import com.app.belcobtm.presentation.features.authorization.recover.wallet.RecoverWalletViewModel
import com.app.belcobtm.presentation.features.pin.code.PinCodeViewModel
import com.app.belcobtm.presentation.features.settings.SettingsViewModel
import com.app.belcobtm.presentation.features.settings.password.PasswordViewModel
import com.app.belcobtm.presentation.features.settings.phone.PhoneChangeViewModel
import com.app.belcobtm.presentation.features.settings.phone.PhoneDisplayViewModel
import com.app.belcobtm.presentation.features.settings.unlink.UnlinkViewModel
import com.app.belcobtm.presentation.features.settings.update_password.UpdatePasswordViewModel
import com.app.belcobtm.presentation.features.settings.verification.blank.VerificationBlankViewModel
import com.app.belcobtm.presentation.features.settings.verification.info.VerificationInfoViewModel
import com.app.belcobtm.presentation.features.settings.verification.vip.VerificationVipViewModel
import com.app.belcobtm.presentation.features.sms.code.SmsCodeViewModel
import com.app.belcobtm.presentation.features.wallet.add.ManageWalletsViewModel
import com.app.belcobtm.presentation.features.wallet.balance.WalletViewModel
import com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin.ExchangeViewModel
import com.app.belcobtm.presentation.features.wallet.send.gift.SendGiftViewModel
import com.app.belcobtm.presentation.features.wallet.staking.StakingViewModel
import com.app.belcobtm.presentation.features.wallet.trade.create.TradeCreateViewModel
import com.app.belcobtm.presentation.features.wallet.trade.details.TradeDetailsBuyViewModel
import com.app.belcobtm.presentation.features.wallet.trade.edit.TradeEditViewModel
import com.app.belcobtm.presentation.features.wallet.trade.main.TradeViewModel
import com.app.belcobtm.presentation.features.wallet.trade.recall.TradeRecallViewModel
import com.app.belcobtm.presentation.features.wallet.trade.reserve.TradeReserveViewModel
import com.app.belcobtm.presentation.features.wallet.transaction.details.TransactionDetailsViewModel
import com.app.belcobtm.presentation.features.wallet.transactions.TransactionsViewModel
import com.app.belcobtm.presentation.features.wallet.withdraw.WithdrawViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { WalletViewModel(get()) }
    viewModel { (coinCode: String) -> TransactionsViewModel(coinCode, get(), get(), get(), get()) }
    viewModel { RecoverWalletViewModel(get()) }
    viewModel { CreateWalletViewModel(get()) }
    viewModel { PinCodeViewModel(get(), get(), get()) }
    viewModel { VerificationInfoViewModel(get()) }
    viewModel { VerificationBlankViewModel(get(), get()) }
    viewModel { VerificationVipViewModel(get()) }
    viewModel { (coinCode: String) ->
        val coinList = (get() as GetCoinListUseCase).invoke()
        val fromCoinDataItem = coinList.find { it.code == coinCode }!!
        val feeMap = get<WalletRepository>().getCoinFeeMap()
        val fromCoinFee = feeMap[coinCode] ?: error("")
        ExchangeViewModel(
            get(),
            fromCoinDataItem,
            coinList,
            fromCoinFee,
            feeMap
        )
    }
    viewModel { ManageWalletsViewModel(get(), get()) }
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
    viewModel { RecoverSeedViewModel(get()) }
    viewModel { CreateSeedViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { PasswordViewModel(get(), get()) }
    viewModel { UnlinkViewModel(get()) }
    viewModel { UpdatePasswordViewModel(get()) }
    viewModel { PhoneDisplayViewModel(get(), get()) }
    viewModel { PhoneChangeViewModel(get(), get(), get()) }
    viewModel { AtmViewModel(get()) }
    viewModel { (txId: String, coinCode: String) -> TransactionDetailsViewModel(txId, coinCode, get()) }
    viewModel { (coinCode: String) ->
        val coinList = (get() as GetCoinListUseCase).invoke()
        val fromCoinDataItem = coinList.find { it.code == coinCode }!!
        val fromCoinFee = get<WalletRepository>().getCoinFeeItemByCode(coinCode)
        SendGiftViewModel(get(), fromCoinDataItem, fromCoinFee)
    }
    viewModel { (coinCode: String) ->
        val coinList = (get() as GetCoinListUseCase).invoke()
        val fromCoinDataItem = coinList.find { it.code == coinCode }
        val fromCoinFee = get<WalletRepository>().getCoinFeeItemByCode(coinCode)
        WithdrawViewModel(get(), fromCoinDataItem, fromCoinFee, coinList)
    }
}
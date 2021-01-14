package com.app.belcobtm.presentation.di

import android.content.Context
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.CoinCodeProvider
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.core.formatter.PhoneNumberFormatter
import com.app.belcobtm.presentation.core.validator.PhoneNumberValidator
import com.app.belcobtm.presentation.features.atm.AtmViewModel
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedViewModel
import com.app.belcobtm.presentation.features.authorization.create.wallet.CreateWalletViewModel
import com.app.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedViewModel
import com.app.belcobtm.presentation.features.authorization.recover.wallet.RecoverWalletViewModel
import com.app.belcobtm.presentation.features.contacts.ContactListViewModel
import com.app.belcobtm.presentation.features.deals.swap.SwapViewModel
import com.app.belcobtm.presentation.features.notification.NotificationHelper
import com.app.belcobtm.presentation.features.pin.code.PinCodeViewModel
import com.app.belcobtm.presentation.features.settings.SettingsViewModel
import com.app.belcobtm.presentation.features.settings.about.AboutViewModel
import com.app.belcobtm.presentation.features.settings.password.PasswordViewModel
import com.app.belcobtm.presentation.features.settings.phone.PhoneChangeViewModel
import com.app.belcobtm.presentation.features.settings.security.SecurityViewModel
import com.app.belcobtm.presentation.features.settings.unlink.UnlinkViewModel
import com.app.belcobtm.presentation.features.settings.update_password.UpdatePasswordViewModel
import com.app.belcobtm.presentation.features.settings.verification.blank.VerificationBlankViewModel
import com.app.belcobtm.presentation.features.settings.verification.info.VerificationInfoViewModel
import com.app.belcobtm.presentation.features.settings.verification.vip.VerificationVipViewModel
import com.app.belcobtm.presentation.features.sms.code.SmsCodeViewModel
import com.app.belcobtm.presentation.features.wallet.add.WalletsViewModel
import com.app.belcobtm.presentation.features.wallet.balance.WalletViewModel
import com.app.belcobtm.presentation.features.wallet.deposit.DepositViewModel
import com.app.belcobtm.presentation.features.wallet.send.gift.SendGiftViewModel
import com.app.belcobtm.presentation.features.deals.staking.StakingViewModel
import com.app.belcobtm.presentation.features.wallet.trade.create.TradeCreateViewModel
import com.app.belcobtm.presentation.features.wallet.trade.details.TradeDetailsBuyViewModel
import com.app.belcobtm.presentation.features.wallet.trade.edit.TradeEditViewModel
import com.app.belcobtm.presentation.features.wallet.trade.main.TradeViewModel
import com.app.belcobtm.presentation.features.wallet.trade.recall.TradeRecallViewModel
import com.app.belcobtm.presentation.features.wallet.trade.reserve.TradeReserveViewModel
import com.app.belcobtm.presentation.features.wallet.transaction.details.TransactionDetailsViewModel
import com.app.belcobtm.presentation.features.wallet.transactions.TransactionsViewModel
import com.app.belcobtm.presentation.features.wallet.withdraw.WithdrawViewModel
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.*

val viewModelModule = module {
    viewModel { AboutViewModel(androidApplication(), get()) }
    viewModel { SecurityViewModel(get()) }
    viewModel { WalletViewModel(get(), get(), get()) }
    viewModel { (coinCode: String) -> TransactionsViewModel(coinCode, get(), get(), get(), get()) }
    viewModel { RecoverWalletViewModel(get(), get<PhoneNumberValidator>()) }
    viewModel { CreateWalletViewModel(get(), get<PhoneNumberValidator>()) }
    viewModel { PinCodeViewModel(get(), get(), get(), get(), get()) }
    viewModel { VerificationInfoViewModel(get()) }
    viewModel { VerificationBlankViewModel(get(), get()) }
    viewModel { VerificationVipViewModel(get()) }
    viewModel { SwapViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { WalletsViewModel(get(), get()) }
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
            get<WalletRepository>().getCoinDetailsItemByCode(coinCode),
            get(),
            get()
        )
    }
    viewModel { (coinCode: String) ->
        TradeReserveViewModel(
            get<WalletRepository>().getCoinItemByCode(coinCode),
            get<WalletRepository>().getCoinDetailsItemByCode(coinCode),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        StakingViewModel(get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { (phone: String) ->
        SmsCodeViewModel(phone, get(), get())
    }
    viewModel { RecoverSeedViewModel(get()) }
    viewModel { CreateSeedViewModel(get(), get()) }
    viewModel { SettingsViewModel() }
    viewModel { PasswordViewModel(get(), get()) }
    viewModel { UnlinkViewModel(get()) }
    viewModel { UpdatePasswordViewModel(get()) }
    viewModel { PhoneChangeViewModel(get(), get(), get(), get<PhoneNumberValidator>()) }
    viewModel { AtmViewModel(get()) }
    viewModel { (txId: String, coinCode: String) ->
        TransactionDetailsViewModel(
            txId,
            coinCode,
            get()
        )
    }
    viewModel {
        SendGiftViewModel(get(), get(), get(), get(), get(), get(), get(), get<PhoneNumberValidator>())
    }
    viewModel { (coinCode: String) ->
        val coinList = (get() as GetCoinListUseCase).invoke()
        val fromCoinDataItem = coinList.find { it.code == coinCode }
        val fromCoinFee = get<WalletRepository>().getCoinDetailsItemByCode(coinCode)
        WithdrawViewModel(get(), fromCoinDataItem, fromCoinFee, coinList, get(), get(), get())
    }
    viewModel { (coinCode: String) ->
        DepositViewModel(coinCode, get())
    }
    viewModel {
        ContactListViewModel(get(), get<PhoneNumberValidator>(), get())
    }
}

val viewModelHelperModule = module {
    factory { MinMaxCoinValueProvider() }
    factory { CoinCodeProvider() }
    factory { AmountCoinValidator() }
    factory { PhoneNumberUtil.createInstance(get<Context>()) }
    factory { PhoneNumberValidator(get()) }
    factory<Formatter<String>> { PhoneNumberFormatter(Locale.US.country) }
}

val helperModule = module {
    factory { NotificationHelper(get()) }
}
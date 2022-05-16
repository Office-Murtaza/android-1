package com.belcobtm.presentation.di

import android.content.Context
import com.belcobtm.presentation.core.coin.CoinCodeProvider
import com.belcobtm.presentation.core.formatter.*
import com.belcobtm.presentation.core.formatter.DoubleCurrencyPriceFormatter.Companion.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.formatter.GoogleMapsDirectionQueryFormatter.Companion.GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER
import com.belcobtm.presentation.core.formatter.IntCurrencyPriceFormatter.Companion.INT_CURRENCY_PRICE_FORMATTER_QUALIFIER
import com.belcobtm.presentation.core.formatter.MilesFormatter.Companion.MILES_FORMATTER_QUALIFIER
import com.belcobtm.presentation.core.helper.ClipBoardHelper
import com.belcobtm.presentation.core.parser.DistanceParser
import com.belcobtm.presentation.core.parser.DistanceParser.Companion.DISTANCE_INT_PARSER_QUALIFIER
import com.belcobtm.presentation.core.parser.PriceDoubleParser
import com.belcobtm.presentation.core.parser.PriceDoubleParser.Companion.PRICE_DOUBLE_PARSER_QUALIFIER
import com.belcobtm.presentation.core.parser.StringParser
import com.belcobtm.presentation.core.provider.string.ResourceStringProvider
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.core.validator.PhoneNumberValidator
import com.belcobtm.presentation.features.HostViewModel
import com.belcobtm.presentation.features.atm.AtmViewModel
import com.belcobtm.presentation.features.authorization.create.seed.CreateSeedViewModel
import com.belcobtm.presentation.features.authorization.create.wallet.CreateWalletViewModel
import com.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedViewModel
import com.belcobtm.presentation.features.authorization.recover.wallet.RecoverWalletViewModel
import com.belcobtm.presentation.features.contacts.ContactListViewModel
import com.belcobtm.presentation.features.deals.DealsViewModel
import com.belcobtm.presentation.features.deals.atm.sell.AtmSellViewModel
import com.belcobtm.presentation.features.deals.staking.StakingViewModel
import com.belcobtm.presentation.features.deals.swap.SwapViewModel
import com.belcobtm.presentation.features.notification.NotificationHelper
import com.belcobtm.presentation.features.pin.code.PinCodeViewModel
import com.belcobtm.presentation.features.referral.ReferralViewModel
import com.belcobtm.presentation.features.referral.contacts.InviteFromContactsViewModel
import com.belcobtm.presentation.features.settings.SettingsViewModel
import com.belcobtm.presentation.features.settings.about.AboutViewModel
import com.belcobtm.presentation.features.settings.password.PasswordViewModel
import com.belcobtm.presentation.features.settings.phone.PhoneChangeViewModel
import com.belcobtm.presentation.features.settings.security.SecurityViewModel
import com.belcobtm.presentation.features.settings.unlink.UnlinkViewModel
import com.belcobtm.presentation.features.settings.update_password.UpdatePasswordViewModel
import com.belcobtm.presentation.features.settings.verification.blank.VerificationBlankViewModel
import com.belcobtm.presentation.features.settings.verification.details.VerificationDetailsViewModel
import com.belcobtm.presentation.features.settings.verification.vip.VerificationVipViewModel
import com.belcobtm.presentation.features.sms.code.SmsCodeViewModel
import com.belcobtm.presentation.features.wallet.add.WalletsViewModel
import com.belcobtm.presentation.features.wallet.balance.WalletViewModel
import com.belcobtm.presentation.features.wallet.deposit.DepositViewModel
import com.belcobtm.presentation.features.wallet.send.gift.SendGiftViewModel
import com.belcobtm.presentation.features.wallet.trade.container.TradeContainerViewModel
import com.belcobtm.presentation.features.wallet.trade.create.CreateTradeViewModel
import com.belcobtm.presentation.features.wallet.trade.details.TradeDetailsViewModel
import com.belcobtm.presentation.features.wallet.trade.edit.EditTradeViewModel
import com.belcobtm.presentation.features.wallet.trade.list.TradeListViewModel
import com.belcobtm.presentation.features.wallet.trade.list.filter.TradeFilterViewModel
import com.belcobtm.presentation.features.wallet.trade.mytrade.details.MyTradeDetailsViewModel
import com.belcobtm.presentation.features.wallet.trade.mytrade.list.MyTradesViewModel
import com.belcobtm.presentation.features.wallet.trade.order.TradeOrdersViewModel
import com.belcobtm.presentation.features.wallet.trade.order.chat.OrderChatViewModel
import com.belcobtm.presentation.features.wallet.trade.order.create.TradeCreateOrderViewModel
import com.belcobtm.presentation.features.wallet.trade.order.details.TradeOrderDetailsViewModel
import com.belcobtm.presentation.features.wallet.trade.order.historychat.HistoryChatViewModel
import com.belcobtm.presentation.features.wallet.trade.order.rate.TradeOrderRateViewModel
import com.belcobtm.presentation.features.wallet.trade.recall.TradeRecallViewModel
import com.belcobtm.presentation.features.wallet.trade.reserve.TradeReserveViewModel
import com.belcobtm.presentation.features.wallet.trade.statistic.TradeUserStatisticViewModel
import com.belcobtm.presentation.features.wallet.transaction.details.TransactionDetailsViewModel
import com.belcobtm.presentation.features.wallet.transactions.TransactionsViewModel
import com.belcobtm.presentation.features.wallet.withdraw.WithdrawViewModel
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.*

val viewModelModule = module {
    viewModel { AboutViewModel(get()) }
    viewModel { SecurityViewModel(get(), get(), get(), get(), get()) }
    viewModel { WalletViewModel(get(), get(), get(), get()) }
    viewModel { (coinCode: String) -> TransactionsViewModel(coinCode, get(), get(), get(), get()) }
    viewModel { RecoverWalletViewModel(get(), get<PhoneNumberValidator>()) }
    viewModel { CreateWalletViewModel(get(), get<PhoneNumberValidator>()) }
    viewModel {
        PinCodeViewModel(
            get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get(),
            get(), get(), get(), get()
        )
    }
    viewModel {
        VerificationDetailsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    viewModel { VerificationBlankViewModel(get()) }
    viewModel { VerificationVipViewModel(get()) }
    viewModel {
        SwapViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { WalletsViewModel(get(), get()) }
    viewModel { (coinCode: String) -> TradeRecallViewModel(coinCode, get(), get(), get(), get()) }
    viewModel { (coinCode: String) ->
        TradeReserveViewModel(
            coinCode, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
    viewModel { StakingViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (phone: String) -> SmsCodeViewModel(phone, get(), get()) }
    viewModel { RecoverSeedViewModel(get(), get()) }
    viewModel { CreateSeedViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel() }
    viewModel { DealsViewModel(get()) }
    viewModel { PasswordViewModel(get(), get()) }
    viewModel { UnlinkViewModel(get()) }
    viewModel { UpdatePasswordViewModel(get()) }
    viewModel { PhoneChangeViewModel(get(), get(), get(), get<PhoneNumberValidator>()) }
    viewModel { AtmViewModel(get()) }
    viewModel { (txId: String, coinCode: String) ->
        TransactionDetailsViewModel(
            txId, coinCode, get(), get(),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)), get()
        )
    }
    viewModel {
        SendGiftViewModel(
            get(), get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get(), get()
        )
    }
    viewModel { (coinCode: String) ->
        WithdrawViewModel(
            coinCode, get(), get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
    viewModel { (coinCode: String) -> DepositViewModel(coinCode, get()) }
    viewModel { ContactListViewModel(get(), get<PhoneNumberValidator>(), get()) }
    viewModel { TradeContainerViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { TradeListViewModel(get(), get()) }
    viewModel { TradeUserStatisticViewModel(get()) }
    viewModel { InviteFromContactsViewModel(get(), get(), get(), get()) }
    viewModel { MyTradeDetailsViewModel(get(), get(), get(), get(), get()) }
    viewModel { EditTradeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { TradeOrdersViewModel(get()) }
    viewModel { MyTradesViewModel(get(), get()) }
    viewModel {
        AtmSellViewModel(
            get(), get(), get(), get(), get(),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
        )
    }
    viewModel {
        TradeDetailsViewModel(
            get(), get(),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER))
        )
    }
    viewModel {
        CreateTradeViewModel(get(), get(), get(), get(), get(), get(), get())
    }
    viewModel {
        TradeFilterViewModel(
            get(),
            get(),
            get(),
            get(),
            get(named(DISTANCE_INT_PARSER_QUALIFIER))
        )
    }
    viewModel {
        TradeOrderDetailsViewModel(
            get(), get(), get(), get(), get(), get(), get(),
            get(named(GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER))
        )
    }
    viewModel {
        TradeCreateOrderViewModel(
            get(), get(), get(), get(), get(),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get()
        )
    }
    viewModel { TradeOrderRateViewModel(get(), get()) }
    viewModel { OrderChatViewModel(get(), get(), get(), get()) }
    viewModel { HistoryChatViewModel(get()) }
    viewModel { HostViewModel(get()) }
    viewModel { ReferralViewModel(get(), get()) }
}

val viewModelHelperModule = module {
    factory { CoinCodeProvider() }
    factory { PhoneNumberUtil.createInstance(get<Context>()) }
    factory { PhoneNumberValidator(get()) }
    factory { PhoneNumberFormatter(get<Locale>().country) }
}

val helperModule = module {
    factory { NotificationHelper(get(), get()) }
    single { ClipBoardHelper(androidApplication()) }
    single<Locale> { Locale.US }
    factory<Formatter<Double>>(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)) {
        DoubleCurrencyPriceFormatter(
            get()
        )
    }
    factory<Formatter<Double>>(named(MILES_FORMATTER_QUALIFIER)) { MilesFormatter(get()) }
    factory<Formatter<Int>>(named(INT_CURRENCY_PRICE_FORMATTER_QUALIFIER)) {
        IntCurrencyPriceFormatter(
            get()
        )
    }
    factory<Formatter<GoogleMapsDirectionQueryFormatter.Location>>(
        named(
            GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER
        )
    ) {
        GoogleMapsDirectionQueryFormatter()
    }
    factory<Formatter<Int>>(named(TradeCountFormatter.TRADE_COUNT_FORMATTER_QUALIFIER)) {
        TradeCountFormatter(
            get()
        )
    }
    factory<StringParser<Double>>(named(PRICE_DOUBLE_PARSER_QUALIFIER)) { PriceDoubleParser() }
    factory<StringParser<Int>>(named(DISTANCE_INT_PARSER_QUALIFIER)) { DistanceParser() }
    single<StringProvider> { ResourceStringProvider(androidApplication().resources) }
}

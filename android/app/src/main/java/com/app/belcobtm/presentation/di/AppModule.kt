package com.app.belcobtm.presentation.di

import android.content.Context
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.CoinCodeProvider
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.formatter.PhoneNumberFormatter
import com.app.belcobtm.presentation.core.formatter.*
import com.app.belcobtm.presentation.core.formatter.DoubleCurrencyPriceFormatter.Companion.DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.core.formatter.GoogleMapsDirectionQueryFormatter.Companion.GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER
import com.app.belcobtm.presentation.core.formatter.IntCurrencyPriceFormatter.Companion.INT_CURRENCY_PRICE_FORMATTER_QUALIFIER
import com.app.belcobtm.presentation.core.formatter.MilesFormatter.Companion.MILES_FORMATTER_QUALIFIER
import com.app.belcobtm.presentation.core.helper.ClipBoardHelper
import com.app.belcobtm.presentation.core.parser.DistanceParser
import com.app.belcobtm.presentation.core.parser.DistanceParser.Companion.DISTANCE_INT_PARSER_QUALIFIER
import com.app.belcobtm.presentation.core.parser.PriceDoubleParser
import com.app.belcobtm.presentation.core.parser.PriceDoubleParser.Companion.PRICE_DOUBLE_PARSER_QUALIFIER
import com.app.belcobtm.presentation.core.parser.StringParser
import com.app.belcobtm.presentation.core.provider.string.ResourceStringProvider
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import com.app.belcobtm.presentation.core.validator.PhoneNumberValidator
import com.app.belcobtm.presentation.features.atm.AtmViewModel
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedViewModel
import com.app.belcobtm.presentation.features.authorization.create.wallet.CreateWalletViewModel
import com.app.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedViewModel
import com.app.belcobtm.presentation.features.authorization.recover.wallet.RecoverWalletViewModel
import com.app.belcobtm.presentation.features.contacts.ContactListViewModel
import com.app.belcobtm.presentation.features.deals.staking.StakingViewModel
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
import com.app.belcobtm.presentation.features.wallet.trade.container.TradeContainerViewModel
import com.app.belcobtm.presentation.features.wallet.trade.create.CreateTradeViewModel
import com.app.belcobtm.presentation.features.wallet.trade.details.TradeDetailsViewModel
import com.app.belcobtm.presentation.features.wallet.trade.edit.EditTradeViewModel
import com.app.belcobtm.presentation.features.wallet.trade.list.TradeListViewModel
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.TradeFilterViewModel
import com.app.belcobtm.presentation.features.wallet.trade.mytrade.details.MyTradeDetailsViewModel
import com.app.belcobtm.presentation.features.wallet.trade.mytrade.list.MyTradesViewModel
import com.app.belcobtm.presentation.features.wallet.trade.order.TradeOrdersViewModel
import com.app.belcobtm.presentation.features.wallet.trade.order.chat.OrderChatViewModel
import com.app.belcobtm.presentation.features.wallet.trade.order.create.TradeCreateOrderViewModel
import com.app.belcobtm.presentation.features.wallet.trade.order.details.TradeOrderDetailsViewModel
import com.app.belcobtm.presentation.features.wallet.trade.order.historychat.HistoryChatViewModel
import com.app.belcobtm.presentation.features.wallet.trade.order.rate.TradeOrderRateViewModel
import com.app.belcobtm.presentation.features.wallet.trade.recall.TradeRecallViewModel
import com.app.belcobtm.presentation.features.wallet.trade.reserve.TradeReserveViewModel
import com.app.belcobtm.presentation.features.wallet.trade.statistic.TradeUserStatisticViewModel
import com.app.belcobtm.presentation.features.wallet.transaction.details.TransactionDetailsViewModel
import com.app.belcobtm.presentation.features.wallet.transactions.TransactionsViewModel
import com.app.belcobtm.presentation.features.wallet.withdraw.WithdrawViewModel
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.*

val viewModelModule = module {
    viewModel { AboutViewModel(androidApplication(), get()) }
    viewModel { SecurityViewModel(get(), get(), get(), get(), get()) }
    viewModel { WalletViewModel(get(), get()) }
    viewModel { (coinCode: String) -> TransactionsViewModel(coinCode, get(), get(), get()) }
    viewModel { RecoverWalletViewModel(get(), get<PhoneNumberValidator>()) }
    viewModel { CreateWalletViewModel(get(), get<PhoneNumberValidator>()) }
    viewModel { PinCodeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { VerificationInfoViewModel(get()) }
    viewModel { VerificationBlankViewModel(get(), get()) }
    viewModel { VerificationVipViewModel(get()) }
    viewModel { SwapViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { WalletsViewModel(get(), get()) }
    viewModel { (coinCode: String) ->
        TradeRecallViewModel(coinCode, get(), get(), get(), get(), get())
    }
    viewModel { (coinCode: String) ->
        TradeReserveViewModel(coinCode, get(), get(), get(), get(), get(), get(), get())
    }
    viewModel {
        StakingViewModel(get(), get(), get(), get(), get(), get())
    }
    viewModel { (phone: String) ->
        SmsCodeViewModel(phone, get())
    }
    viewModel { RecoverSeedViewModel(get()) }
    viewModel { CreateSeedViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel() }
    viewModel { PasswordViewModel(get(), get()) }
    viewModel { UnlinkViewModel(get()) }
    viewModel { UpdatePasswordViewModel(get()) }
    viewModel { PhoneChangeViewModel(get(), get(), get(), get<PhoneNumberValidator>()) }
    viewModel { AtmViewModel(get()) }
    viewModel { (txId: String, coinCode: String) -> TransactionDetailsViewModel(txId, coinCode, get(),
            get()
        ) }
    viewModel {
        SendGiftViewModel(get(), get(), get(), get(), get())
    }
    viewModel { (coinCode: String) -> WithdrawViewModel(coinCode, get(), get(), get(), get()) }
    viewModel { (coinCode: String) -> DepositViewModel(coinCode, get()) }
    viewModel { ContactListViewModel(get(), get<PhoneNumberValidator>(), get()) }
    viewModel { TradeContainerViewModel(get(), get(), get(), get()) }
    viewModel { TradeListViewModel(get(), get()) }
    viewModel { TradeUserStatisticViewModel(get()) }
    viewModel { MyTradeDetailsViewModel(get(), get(), get(), get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER))) }
    viewModel {
        EditTradeViewModel(
            get(), get(), get(), get(), get(),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(INT_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(PRICE_DOUBLE_PARSER_QUALIFIER))
        )
    }
    viewModel { TradeOrdersViewModel(get()) }
    viewModel { MyTradesViewModel(get()) }
    viewModel {
        TradeDetailsViewModel(
            get(), get(),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER))
        )
    }
    viewModel {
        CreateTradeViewModel(
            get(), get(), get(), get(), get(),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(INT_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(PRICE_DOUBLE_PARSER_QUALIFIER))
        )
    }
    viewModel { TradeFilterViewModel(get(), get(), get(), get(), get(named(DISTANCE_INT_PARSER_QUALIFIER))) }
    viewModel { TradeOrderDetailsViewModel(get(), get(), get(), get(named(GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER))) }
    viewModel {
        TradeCreateOrderViewModel(
            get(), get(), get(), get(),
            get(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            get(named(PRICE_DOUBLE_PARSER_QUALIFIER))
        )
    }
    viewModel { TradeOrderRateViewModel(get(), get()) }
    viewModel { OrderChatViewModel(get(), get(), get()) }
    viewModel { HistoryChatViewModel(get()) }
}

val viewModelHelperModule = module {
    factory { MinMaxCoinValueProvider() }
    factory { CoinCodeProvider() }
    factory { AmountCoinValidator() }
    factory { PhoneNumberUtil.createInstance(get<Context>()) }
    factory { PhoneNumberValidator(get()) }
    factory { PhoneNumberFormatter(get<Locale>().country) }
}

val helperModule = module {
    factory { NotificationHelper(get()) }
    single { ClipBoardHelper(androidApplication()) }
    single<Locale> { Locale.US }
    factory<Formatter<Double>>(named(DOUBLE_CURRENCY_PRICE_FORMATTER_QUALIFIER)) { DoubleCurrencyPriceFormatter(get()) }
    factory<Formatter<Double>>(named(MILES_FORMATTER_QUALIFIER)) { MilesFormatter(get()) }
    factory<Formatter<Int>>(named(INT_CURRENCY_PRICE_FORMATTER_QUALIFIER)) { IntCurrencyPriceFormatter(get()) }
    factory<Formatter<GoogleMapsDirectionQueryFormatter.Location>>(named(GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER)) {
        GoogleMapsDirectionQueryFormatter()
    }
    factory<Formatter<Int>>(named(TradeCountFormatter.TRADE_COUNT_FORMATTER_QUALIFIER)) { TradeCountFormatter(get()) }
    factory<StringParser<Double>>(named(PRICE_DOUBLE_PARSER_QUALIFIER)) { PriceDoubleParser(get()) }
    factory<StringParser<Int>>(named(DISTANCE_INT_PARSER_QUALIFIER)) { DistanceParser() }
    single<StringProvider> { ResourceStringProvider(androidApplication().resources) }
}
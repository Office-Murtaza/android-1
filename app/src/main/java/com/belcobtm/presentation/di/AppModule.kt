package com.belcobtm.presentation.di

import android.content.Context
import com.belcobtm.presentation.core.coin.CoinCodeProvider
import com.belcobtm.presentation.core.helper.ClipBoardHelper
import com.belcobtm.presentation.core.parser.DistanceParser
import com.belcobtm.presentation.core.parser.DistanceParser.Companion.DISTANCE_INT_PARSER_QUALIFIER
import com.belcobtm.presentation.core.parser.PriceDoubleParser
import com.belcobtm.presentation.core.parser.PriceDoubleParser.Companion.PRICE_DOUBLE_PARSER_QUALIFIER
import com.belcobtm.presentation.core.parser.StringParser
import com.belcobtm.presentation.core.provider.string.ResourceStringProvider
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.screens.HostViewModel
import com.belcobtm.presentation.screens.MainViewModel
import com.belcobtm.presentation.screens.atm.AtmViewModel
import com.belcobtm.presentation.screens.authorization.create.seed.CreateSeedViewModel
import com.belcobtm.presentation.screens.authorization.create.wallet.CreateWalletViewModel
import com.belcobtm.presentation.screens.authorization.recover.seed.RecoverSeedViewModel
import com.belcobtm.presentation.screens.authorization.recover.wallet.RecoverWalletViewModel
import com.belcobtm.presentation.screens.bank_accounts.BankAccountsViewModel
import com.belcobtm.presentation.screens.bank_accounts.ach.BankAchViewModel
import com.belcobtm.presentation.screens.bank_accounts.create.BankAccountCreateViewModel
import com.belcobtm.presentation.screens.bank_accounts.details.BankAccountDetailsViewModel
import com.belcobtm.presentation.screens.bank_accounts.payments.PaymentBuyUsdcViewModel
import com.belcobtm.presentation.screens.bank_accounts.payments.PaymentSellUsdcViewModel
import com.belcobtm.presentation.screens.bank_accounts.payments.PaymentSummaryViewModel
import com.belcobtm.presentation.screens.contacts.ContactListViewModel
import com.belcobtm.presentation.screens.notification.NotificationHelper
import com.belcobtm.presentation.screens.pin.code.PinCodeViewModel
import com.belcobtm.presentation.screens.services.ServicesViewModel
import com.belcobtm.presentation.screens.services.atm.sell.AtmSellViewModel
import com.belcobtm.presentation.screens.services.staking.StakingViewModel
import com.belcobtm.presentation.screens.services.swap.SwapViewModel
import com.belcobtm.presentation.screens.services_info.ServicesInfoViewModel
import com.belcobtm.presentation.screens.settings.SettingsViewModel
import com.belcobtm.presentation.screens.settings.about.AboutViewModel
import com.belcobtm.presentation.screens.settings.referral.ReferralViewModel
import com.belcobtm.presentation.screens.settings.referral.contacts.InviteFromContactsViewModel
import com.belcobtm.presentation.screens.settings.security.SecurityViewModel
import com.belcobtm.presentation.screens.settings.security.password.PasswordViewModel
import com.belcobtm.presentation.screens.settings.security.phone.PhoneChangeViewModel
import com.belcobtm.presentation.screens.settings.security.unlink.UnlinkViewModel
import com.belcobtm.presentation.screens.settings.security.update_password.UpdatePasswordViewModel
import com.belcobtm.presentation.screens.settings.verification.blank.VerificationBlankViewModel
import com.belcobtm.presentation.screens.settings.verification.details.VerificationDetailsViewModel
import com.belcobtm.presentation.screens.settings.wallets.WalletsViewModel
import com.belcobtm.presentation.screens.sms.code.SmsCodeViewModel
import com.belcobtm.presentation.screens.wallet.balance.WalletViewModel
import com.belcobtm.presentation.screens.wallet.deposit.DepositViewModel
import com.belcobtm.presentation.screens.wallet.send.gift.SendGiftViewModel
import com.belcobtm.presentation.screens.wallet.trade.container.TradeContainerViewModel
import com.belcobtm.presentation.screens.wallet.trade.create.CreateTradeViewModel
import com.belcobtm.presentation.screens.wallet.trade.details.TradeDetailsViewModel
import com.belcobtm.presentation.screens.wallet.trade.edit.EditTradeViewModel
import com.belcobtm.presentation.screens.wallet.trade.list.TradeListViewModel
import com.belcobtm.presentation.screens.wallet.trade.list.filter.TradeFilterViewModel
import com.belcobtm.presentation.screens.wallet.trade.mytrade.details.MyTradeDetailsViewModel
import com.belcobtm.presentation.screens.wallet.trade.mytrade.list.MyTradesViewModel
import com.belcobtm.presentation.screens.wallet.trade.order.TradeOrdersViewModel
import com.belcobtm.presentation.screens.wallet.trade.order.chat.OrderChatViewModel
import com.belcobtm.presentation.screens.wallet.trade.order.create.TradeCreateOrderViewModel
import com.belcobtm.presentation.screens.wallet.trade.order.details.TradeOrderDetailsViewModel
import com.belcobtm.presentation.screens.wallet.trade.order.historychat.HistoryChatViewModel
import com.belcobtm.presentation.screens.wallet.trade.order.rate.TradeOrderRateViewModel
import com.belcobtm.presentation.screens.wallet.trade.recall.TradeRecallViewModel
import com.belcobtm.presentation.screens.wallet.trade.reserve.TradeReserveViewModel
import com.belcobtm.presentation.screens.wallet.trade.statistic.TradeUserStatisticViewModel
import com.belcobtm.presentation.screens.wallet.transaction.details.TransactionDetailsViewModel
import com.belcobtm.presentation.screens.wallet.transactions.TransactionsViewModel
import com.belcobtm.presentation.screens.wallet.withdraw.WithdrawViewModel
import com.belcobtm.presentation.tools.formatter.CryptoPriceFormatter
import com.belcobtm.presentation.tools.formatter.CryptoPriceFormatter.Companion.CRYPTO_PRICE_FORMATTER_QUALIFIER
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.CurrencyPriceFormatter.Companion.CURRENCY_PRICE_FORMATTER_QUALIFIER
import com.belcobtm.presentation.tools.formatter.Formatter
import com.belcobtm.presentation.tools.formatter.GoogleMapsDirectionQueryFormatter
import com.belcobtm.presentation.tools.formatter.GoogleMapsDirectionQueryFormatter.Companion.GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER
import com.belcobtm.presentation.tools.formatter.IntCurrencyPriceFormatter
import com.belcobtm.presentation.tools.formatter.IntCurrencyPriceFormatter.Companion.INT_CURRENCY_PRICE_FORMATTER_QUALIFIER
import com.belcobtm.presentation.tools.formatter.MilesFormatter
import com.belcobtm.presentation.tools.formatter.MilesFormatter.Companion.MILES_FORMATTER_QUALIFIER
import com.belcobtm.presentation.tools.formatter.PhoneNumberFormatter
import com.belcobtm.presentation.tools.formatter.TradeCountFormatter
import com.belcobtm.presentation.tools.validator.PhoneNumberValidator
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.Locale

val viewModelModule = module {
    viewModel {
        MainViewModel(
            connectToSocketUseCase = get(),
            connectToWalletUseCase = get(),
            connectToBankAccountsUseCase = get(),
            connectToPaymentsUseCase = get(),
            connectToTransactionsUseCase = get(),
            connectToServicesUseCase = get(),
            connectToTradesDataUseCase = get(),
            connectToOrdersDataUseCase = get(),
            connectToChatUseCase = get(),
        )
    }
    viewModel { BankAccountCreateViewModel(get()) }
    viewModel {
        BankAccountsViewModel(
            getBankAccountsListUseCase = get(),
            observeBankAccountsListUseCase = get(),
            preferences = get()
        )
    }
    viewModel { (bankAccountId: String) ->
        BankAccountDetailsViewModel(
            bankAccountId,
            get(),
            get(),
            get(),
        )
    }
    viewModel {
        BankAchViewModel(
            getLinkTokenUseCase = get(),
            linkBankAccountUseCase = get(),
            preferences = get()
        )
    }
    viewModel { PaymentSellUsdcViewModel(get(), get()) }
    viewModel { PaymentBuyUsdcViewModel(get(), get()) }
    viewModel { PaymentSummaryViewModel(get(), get(), get(), get()) }
    viewModel { AboutViewModel(get()) }
    viewModel {
        SecurityViewModel(
            phoneNumberFormatter = get(),
            setBioAuthStateAllowedUseCase = get(),
            bioAuthAllowedByUserUseCase = get(),
            bioAuthSupportedByPhoneUseCase = get(),
            updatePhoneUseCase = get(),
            preferences = get()
        )
    }
    viewModel { WalletViewModel(get(), get(), get(), get()) }
    viewModel { (coinCode: String) -> TransactionsViewModel(coinCode, get(), get(), get(), get()) }
    viewModel { RecoverWalletViewModel(get(), get<PhoneNumberValidator>()) }
    viewModel { CreateWalletViewModel(get(), get<PhoneNumberValidator>()) }
    viewModel {
        PinCodeViewModel(
            authorizeUseCase = get(),
            unlinkUseCase = get(),
            bioAuthSupportedByPhoneUseCase = get(),
            bioAuthAllowedByUserUseCase = get(),
            authorizePinUseCase = get(),
            savePinCodeUseCase = get(),
            saveUserAuthedUseCase = get(),
            supportChatInteractor = get()
        )
    }
    viewModel {
        VerificationDetailsViewModel(
            sendVerificationDocumentUseCase = get(),
            sendVerificationIdentityUseCase = get(),
            getVerificationDetailsUseCase = get(),
            countriesUseCase = get(),
            preferences = get(),
        )
    }
    viewModel { VerificationBlankViewModel(get()) }
    viewModel {
        SwapViewModel(
            accountDao = get(),
            getCoinListUseCase = get(),
            swapUseCase = get(),
            serviceInfoProvider = get(),
            getTransactionPlanUseCase = get(),
            getSignedTransactionPlanUseCase = get(),
            receiverAccountActivatedUseCase = get(),
            getFakeSignedTransactionPlanUseCase = get(),
            getMaxValueBySignedTransactionUseCase = get(),
            stringProvider = get()
        )
    }
    viewModel { WalletsViewModel(get(), get()) }
    viewModel { (coinCode: String) -> TradeRecallViewModel(coinCode, get(), get(), get(), get()) }
    viewModel { (coinCode: String) ->
        TradeReserveViewModel(
            coinCode, get(), get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
    viewModel { StakingViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (phone: String) -> SmsCodeViewModel(phone, get(), get()) }
    viewModel { RecoverSeedViewModel(get(), get()) }
    viewModel { CreateSeedViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel() }
    viewModel { ServicesViewModel(get()) }
    viewModel { PasswordViewModel(get(), get()) }
    viewModel { UnlinkViewModel(get(), get()) }
    viewModel { UpdatePasswordViewModel(get()) }
    viewModel { PhoneChangeViewModel(get(), get(), get<PhoneNumberValidator>()) }
    viewModel { AtmViewModel(get()) }
    viewModel { (transactionId: String, coinCode: String) ->
        TransactionDetailsViewModel(
            transactionId, coinCode, get(), get(),
            get(named(CURRENCY_PRICE_FORMATTER_QUALIFIER)), get()
        )
    }
    viewModel {
        SendGiftViewModel(
            get(), get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get()
        )
    }
    viewModel { (coinCode: String) ->
        WithdrawViewModel(
            coinCode, get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
    viewModel { (coinCode: String) -> DepositViewModel(coinCode, get()) }
    viewModel { ContactListViewModel(get(), get<PhoneNumberValidator>(), get()) }
    viewModel { TradeContainerViewModel(get(), get()) }
    viewModel { TradeListViewModel(get(), get()) }
    viewModel { TradeUserStatisticViewModel(get()) }
    viewModel { InviteFromContactsViewModel(get(), get(), get(), get()) }
    viewModel { MyTradeDetailsViewModel(get(), get(), get()) }
    viewModel { EditTradeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { TradeOrdersViewModel(get()) }
    viewModel { MyTradesViewModel(get(), get()) }
    viewModel {
        AtmSellViewModel(
            getCoinListUseCase = get(),
            sellUseCase = get(),
            accountDao = get(),
            serviceInfoProvider = get(),
            stringProvider = get(),
            priceFormatter = get(named(CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            preferences = get()
        )
    }
    viewModel {
        TradeDetailsViewModel(
            observeTradeDetailsUseCase = get(),
            stringProvider = get(),
            priceFormatter = get(named(CURRENCY_PRICE_FORMATTER_QUALIFIER)),
            googleMapQueryFormatter = get(named(GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER))
        )
    }
    viewModel {
        CreateTradeViewModel(get(), get(), get(), get(), get(), get())
    }
    viewModel {
        TradeFilterViewModel(
            loadFilterDataUseCase = get(),
            resetFilterUseCase = get(),
            applyFilterUseCase = get(),
            stringProvider = get(),
            distanceParser = get(named(DISTANCE_INT_PARSER_QUALIFIER))
        )
    }
    viewModel {
        TradeOrderDetailsViewModel(
            observeMissedMessageCountUseCase = get(),
            observeOrderDetailsUseCase = get(),
            updateOrderStatusUseCase = get(),
            cancelOrderUseCase = get(),
            stringProvider = get(),
            googleMapQueryFormatter = get(named(GOOGLE_MAPS_DIRECTIONS_QUERY_FORMATTER))
        )
    }
    viewModel {
        TradeCreateOrderViewModel(
            getTradeDetailsUseCase = get(),
            getCoinByCodeUseCase = get(),
            createOrderUseCase = get(),
            stringProvider = get(),
            serviceInfoProvider = get(),
            priceFormatter = get(named(CURRENCY_PRICE_FORMATTER_QUALIFIER))
        )
    }
    viewModel { TradeOrderRateViewModel(get(), get()) }
    viewModel { OrderChatViewModel(get(), get(), get(), get()) }
    viewModel { HistoryChatViewModel(get()) }
    viewModel { HostViewModel(get()) }
    viewModel { ReferralViewModel(get(), get()) }

    viewModel { ServicesInfoViewModel(get()) }
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
    factory<Formatter<Double>>(named(CURRENCY_PRICE_FORMATTER_QUALIFIER)) {
        CurrencyPriceFormatter(
            get()
        )
    }
    factory<Formatter<Double>>(named(CRYPTO_PRICE_FORMATTER_QUALIFIER)) {
        CryptoPriceFormatter(
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

import Foundation
import TrustWalletCore

enum ExchangePickerOption {
    case coins
    case none
}

enum CoinExchangeAction: Equatable {
    case setupCoin(BTMCoin)
    case setupCoinBalances([CoinBalance])
    case setupCoinDetails(CoinDetails)
    case updateFromCoinAmount(String?)
    case updateToCoinAmount(String?)
    case updateToCoinType(CustomCoinType)
    case updateFromCoinType(CustomCoinType)
    case updateFromCoinAmountError(String?)
    case updateToCoinTypeError(String?)
    case updateValidationState
    case updateToCoinDetails(CoinDetails)
    case finishFetchingCoinsData(CoinsBalance, CoinDetails, [BTMCoin])
    case updateFromCoinDetails(CoinDetails)
    case updateFromCoin(BTMCoin)
    case isCoinActivated(Bool)
    case swap
}

struct CoinExchangeState: Equatable {
    
    var fromCoin: BTMCoin?
    var toCoinType: CustomCoinType?
    var fromCoinType: CustomCoinType?
    var coinBalances: [CoinBalance]?
    var coinDetails: CoinDetails?
    var toCoinDetails: CoinDetails?
    var fromCoinAmount: String = ""
    var fromCoinAmountError: String?
    var toCoinTxFee: Decimal?
    var toCoinTypeError: String?
    var validationState: ValidationState = .unknown
    var fromRate: String?
    var toRate: String?
    var platformFee: String?
    var coins: [BTMCoin]?
    var isCoinActivated: Bool?
    var toCoinAmountBackConvertation: String?
    
    var coinBalance: CoinsBalance?
    
    
    var fromCoinFiatAmount: String {
        let fromCoinAmountDecimal = fromCoinAmount.decimalValue ?? 0
        let price = fromCoinBalance?.price ?? 0
        
        return (fromCoinAmountDecimal * price).fiatFormatted.withDollarSign
    }
    
    var toCoinAmount: String {

        guard toCoinAmountBackConvertation == nil else {
            return toCoinAmountBackConvertation?.coinFormatted ?? 0.0.coinFormatted
        }
        
        guard
            let fromCoinAmountDecimal = fromCoinAmount.decimalValue,
            let fromCoinPrice = fromCoinBalance?.price, let toCoinPrice = toCoinBalance?.price,
            let profitExchange = coinDetails?.swapProfitPercent
        else {
            return 0.0.coinFormatted
        }
        let toCoinAmountDecimal = fromCoinAmountDecimal * fromCoinPrice / toCoinPrice * (100 - profitExchange) / 100
        
        return toCoinAmountDecimal.coinFormatted(fractionDigits: toCoinDetails?.scale)
    }
    
    func toCoinAmount(amount: Decimal) -> String {
        guard let toCoinType = toCoinType else { return "" }
        
        guard
            let fromCoinPrice = fromCoinBalance?.price, let toCoinPrice = toCoinBalance?.price,
            let profitExchange = coinDetails?.swapProfitPercent
        else {
            return 0.0.coinFormatted.withCoinType(toCoinType)
        }
        let toCoinAmountDecimal = amount * fromCoinPrice / toCoinPrice * (100 - profitExchange) / 100
        
        return toCoinAmountDecimal.coinFormatted(fractionDigits:nil).withCoinType(toCoinType)
    }
    
    var maxFromValue: Decimal {
        guard let type = fromCoinType, let balance = fromCoinBalance?.balance, let fee = coinDetails?.txFee else { return 0 }
        
        switch type {
        case .catm:
            return balance
        case .ripple:
            return max(0, balance - fee - 20)
        default:
            return max(0, balance - fee)
        }
    }
    
    var maxToValue: Decimal {
        guard let type = toCoinType, let balance = toCoinBalance?.balance, let fee = toCoinDetails?.txFee else { return 0 }
        
        switch type {
        case .catm:
            return balance
        case .ripple:
            return max(0, balance - fee - 20)
        default:
            return max(0, balance - fee)
        }
    }
    
    var fromCoinBalance: CoinBalance? {
        return coinBalances?.first { $0.type == fromCoinType}
    }
    
    var toCoinBalance: CoinBalance? {
        return coinBalances?.first { $0.type == toCoinType }
    }
    
    var otherCoinBalances: [CoinBalance]? {
        return coinBalances//?.filter { $0.type != fromCoin?.type }
    }
    
    var fromCoinBalances: [BTMCoin]? {
        return coins?.filter{ $0.type != toCoinType }
    }
    
    var toCoinBalances: [BTMCoin]? {
        return coins?.filter{ $0.type != fromCoinType }
    }
    
    var isAllFieldsNotEmpty: Bool {
        return fromCoinAmount.count > 0 && toCoinType != nil
    }
    
}

final class CoinExchangeStore: ViewStore<CoinExchangeAction, CoinExchangeState> {
    
    override var initialState: CoinExchangeState {
        return CoinExchangeState()
    }
    
    override func reduce(state: CoinExchangeState, action: CoinExchangeAction) -> CoinExchangeState {
        var state = state
        
        switch action {
        case let .setupCoin(coin):
            state.fromCoin = coin
            state.toCoinType = state.coinBalances?.first(where: { $0.type != coin.type })?.type
        case let .setupCoinBalances(coinBalances):
            state.coinBalances = coinBalances
            state.toCoinType = coinBalances.first(where: { $0.type != state.fromCoin?.type })?.type
        case let .setupCoinDetails(coinDetails):
            state.coinDetails = coinDetails
        case let .updateFromCoinAmount(amount):
            state.toCoinAmountBackConvertation = nil
            state.fromCoinAmount = (amount ?? "").coinWithdrawFormatted
            state.fromCoinAmountError = nil
            state.toCoinTypeError = nil
        case let .updateToCoinAmount(amount):
            state.toCoinAmountBackConvertation = amount
            state.fromCoinAmount = backConvertation(state: &state, amount: amount ?? "")
            state.fromCoinAmountError = nil
            state.toCoinTypeError = nil
        case let .updateToCoinType(coinType):
            state.toCoinType = coinType
            state.fromCoinAmount = "".coinWithdrawFormatted
            updateRateViewState(state: &state, amount: 1)
            state.fromCoinAmountError = nil
            state.toCoinTypeError = nil
        case let .updateFromCoinType(coinType):
            state.fromCoinType = coinType;
            state.fromCoinAmount = "".coinWithdrawFormatted
        updateRateViewState(state: &state, amount: 1)
            state.fromCoinAmountError = nil
            state.toCoinTypeError = nil
        case let .updateFromCoinAmountError(fromCoinAmountError): state.fromCoinAmountError = fromCoinAmountError
        case let .updateToCoinTypeError(toCoinTypeError): state.toCoinTypeError = toCoinTypeError
        case .updateValidationState: validate(&state)
        case let .updateToCoinDetails(details):
          state.toCoinDetails = details
          state.toCoinTxFee = details.txFee
          updatePlatformFee(state: &state)
        updateRateViewState(state: &state, amount: 1)
        case let .updateFromCoinDetails(details):
            state.coinDetails = details
        case let .finishFetchingCoinsData(balances, details, coins):
            state.coinBalance = balances
            state.coinBalances = balances.coins
            state.coins = coins
            state.coinDetails = details
            let firstCoin = balances.coins.first
            if let type = firstCoin?.type, let address = firstCoin?.address {
                let coin = BTMCoin(type: type, privateKey: "", address: address)
                state.fromCoin = coin
                state.fromCoinType = coin.type
            }
            state.toCoinType = balances.coins.first(where: { $0.type != firstCoin?.type })?.type
        case .swap:
            state.fromCoinAmount = "".coinWithdrawFormatted
            let toCoinType = state.toCoinType
            state.toCoinType = state.fromCoinType
            state.fromCoinType = toCoinType
            updatePlatformFee(state: &state)
            state.fromCoinAmountError = nil
            state.toCoinTypeError = nil
        case let .updateFromCoin(coin):
            state.fromCoin = coin
        case let .isCoinActivated(isActive):
            state.isCoinActivated = isActive
        }
        
        return state
    }
    
    private func updatePlatformFee(state: inout CoinExchangeState) {
        if let details = state.toCoinDetails {
            state.platformFee = platformFeeString(details: details)
        }
    }
    
    private func updateRateViewState(state:inout CoinExchangeState, amount: Int ) {
        state.fromRate = "1".coinFormatted.withCoinType(state.fromCoinType ?? .bitcoin)
        state.toRate = state.toCoinAmount(amount: 1)
    }
    
    private func backConvertation(state: inout CoinExchangeState, amount: String) -> String {
        guard let feeB = state.toCoinDetails?.txFee ?? state.toCoinDetails?.convertedTxFee,
              let priceB = state.toCoinBalance?.price,
              let priceA = state.fromCoinBalance?.price,
              let profit = state.toCoinDetails?.swapProfitPercent else { return 0.0.coinFormatted }
        
        let firstExpression = (amount.decimalValue ?? 0 + feeB) * priceB
        let secondPart = priceA / (1 - profit / 100)
        let result = firstExpression / secondPart
        
        return result.coinFormatted
    }
    
    private func validate(_ state: inout CoinExchangeState) {
        state.validationState = .valid
        let lessThanTxFee = state.fromCoinAmount.decimalValue?.lessThan(state.coinDetails?.txFee ?? 0) == true
            || state.toCoinAmount.decimalValue?.lessThan(state.toCoinTxFee ?? 0) == true
        
        if state.fromCoinAmount.count == 0 {
            setupState(with: &state, errorString: L.CreateWallet.Form.Error.fieldRequired)
        } else if state.fromCoinAmount.decimalValue == nil {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.invalidAmount)
        } else if state.fromCoinAmount.decimalValue! <= 0 {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooLowAmount)
        } else if lessThanTxFee {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.lessThanFee)
        } else if !state.fromCoinAmount.decimalValue!.lessThanOrEqualTo(state.maxFromValue) {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooHighAmount)
        } else if state.toCoinType == .ripple,
                  state.isCoinActivated == false,
                  (state.toCoinAmount.decimalValue ?? 0 >= (20 + (state.toCoinDetails?.txFee ?? 0.0) )) {
            let errorString = localize(L.CoinWithdraw.Form.Error.insufficientETHBalance)
            state.toCoinTypeError = errorString
            state.validationState = .invalid(errorString)
        } else {
            state.fromCoinAmountError = nil
            
            if state.fromCoin?.type.isETHBased ?? false, let fee = state.coinDetails?.txFee {
                let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
                
                if !ethBalance.greaterThanOrEqualTo(fee) {
                    setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.insufficientETHBalance)

                }
            }
        }
        
        if state.toCoinType == nil {
            let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
            state.toCoinTypeError = errorString
            state.validationState = .invalid(errorString)
        }
    }
    
    private func setupState(with state: inout CoinExchangeState, errorString: String) {
        let errorString = localize(errorString)
        state.fromCoinAmountError = errorString
        state.validationState = .invalid(errorString)
    }
    
    private func platformFeeString(details: CoinDetails) -> String {
        let percent =  "\(details.swapProfitPercent)%"
        let fee = "\(details.txFee.coinFormatted.withCoinType(details.type))"
        return "\(percent) ~ \(fee)"
    }
}

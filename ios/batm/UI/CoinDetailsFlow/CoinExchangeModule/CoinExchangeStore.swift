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
    case updateToCoinType(CustomCoinType)
    case updateToCoinTxFee(Decimal)
    case updateFromCoinAmountError(String?)
    case updateToCoinTypeError(String?)
    case updateValidationState
}

struct CoinExchangeState: Equatable {
    
    var fromCoin: BTMCoin?
    var toCoinType: CustomCoinType?
    var coinBalances: [CoinBalance]?
    var coinDetails: CoinDetails?
    var fromCoinAmount: String = ""
    var fromCoinAmountError: String?
    var toCoinTxFee: Decimal?
    var toCoinTypeError: String?
    var validationState: ValidationState = .unknown
    
    var fromCoinFiatAmount: String {
        let fromCoinAmountDecimal = fromCoinAmount.decimalValue ?? 0
        let price = fromCoinBalance?.price ?? 0
        
        return (fromCoinAmountDecimal * price).fiatFormatted.withDollarSign
    }
    
    var toCoinAmount: String {
        guard let toCoinType = toCoinType else { return "" }
        
        guard
            let fromCoinAmountDecimal = fromCoinAmount.decimalValue,
            let fromCoinPrice = fromCoinBalance?.price, let toCoinPrice = toCoinBalance?.price,
            let profitExchange = coinDetails?.profitExchange
        else {
            return 0.0.coinFormatted.withCoinType(toCoinType)
        }
        let toCoinAmountDecimal = fromCoinAmountDecimal * fromCoinPrice / toCoinPrice * (100 - profitExchange) / 100
        return toCoinAmountDecimal.coinFormatted(fractionDigits: coinDetails?.scale).withCoinType(toCoinType)
    }
    
    var maxValue: Decimal {
        guard let type = fromCoin?.type, let balance = fromCoinBalance?.balance, let fee = coinDetails?.txFee else { return 0 }
        
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
        return coinBalances?.first { $0.type == fromCoin?.type }
    }
    
    var toCoinBalance: CoinBalance? {
        return coinBalances?.first { $0.type == toCoinType }
    }
    
    var otherCoinBalances: [CoinBalance]? {
        return coinBalances?.filter { $0.type != fromCoin?.type }
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
        case let .setupCoinDetails(coinDetails): state.coinDetails = coinDetails
        case let .updateFromCoinAmount(amount):
            state.fromCoinAmount = (amount ?? "").coinWithdrawFormatted
            state.fromCoinAmountError = nil
        case let .updateToCoinType(coinType):
            state.toCoinType = coinType
            state.toCoinTypeError = nil
        case let .updateToCoinTxFee(txFee):
            state.toCoinTxFee = txFee
        case let .updateFromCoinAmountError(fromCoinAmountError): state.fromCoinAmountError = fromCoinAmountError
        case let .updateToCoinTypeError(toCoinTypeError): state.toCoinTypeError = toCoinTypeError
        case .updateValidationState: validate(&state)
        }
        
        return state
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
        } else if !state.fromCoinAmount.decimalValue!.lessThanOrEqualTo(state.maxValue) {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooHighAmount)
        } else {
            state.fromCoinAmountError = nil
            
            if state.fromCoin?.type == .catm, let fee = state.coinDetails?.txFee {
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
}

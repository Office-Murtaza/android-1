import Foundation

enum ReserveAction: Equatable {
    case setupCoin(BTMCoin)
    case setupCoinBalances([CoinBalance]?, CustomCoinType)
    case setupCoinDetails(CoinDetails)
    case setupTransactionDetails(TransactionDetails)
    case updateCurrencyAmount(String?)
    case updateCoinAmount(String?)
    case updateValidationState
    case makeInvalidState(String)
}

struct ReserveState: Equatable {
    
    var coin: BTMCoin?
    var coinBalance: CoinBalance?
    var coinBalances: [CoinBalance]?
    var coinDetails: CoinDetails?
    var transactionDetails: TransactionDetails?
    var coinAmount: String = ""
    var coinAmountError: String?
    var validationState: ValidationState = .unknown
    
    var currencyAmount: String {
        let coinAmountDecimal = coinAmount.decimalValue ?? 0
        let price = coinBalance?.price ?? 0
        
        return (coinAmountDecimal * price).fiatFormatted.withDollarSign
    }
    
    var maxValue: Decimal {
        guard let type = coin?.type, let balance = coinBalance?.balance, let fee = coinDetails?.txFee else { return 0 }
        
        switch type {
        case .catm:
            return balance
        case .ripple:
            return max(0, balance - fee - 20)
        default:
            return max(0, balance - fee)
        }
    }
    
    var isFieldNotEmpty: Bool {
        return coinAmount.isNotEmpty
    }
}

final class ReserveStore: ViewStore<ReserveAction, ReserveState> {
    override var initialState: ReserveState {
        return ReserveState()
    }
    
    override func reduce(state: ReserveState, action: ReserveAction) -> ReserveState {
        var state = state
        
        switch action {
        case let .setupCoin(coin): state.coin = coin
        case let .setupCoinBalances(coinBalances, coinType):
            state.coinBalances = coinBalances
            state.coinBalance = coinBalances?.first { $0.type == coinType }
        case let .setupCoinDetails(coinDetails): state.coinDetails = coinDetails
        case let .setupTransactionDetails(transactionDetails): state.transactionDetails = transactionDetails
        case let .updateCurrencyAmount(amount):
            let currencyAmount = (amount ?? "").fiatWithdrawFormatted.withDollarSign
            let decimalCurrencyAmount = currencyAmount.decimalValue
            let price = state.coinBalance!.price
            let coinAmount = decimalCurrencyAmount == nil ? "" : (decimalCurrencyAmount! / price).coinFormatted
            
            state.coinAmount = coinAmount
        case let .updateCoinAmount(amount):
            let coinAmount = (amount ?? "").coinWithdrawFormatted
            state.coinAmount = coinAmount
            state.coinAmountError = nil
        case .updateValidationState: validate(&state)
        case let .makeInvalidState(error): state.validationState = .invalid(error)
        }
        
        return state
    }
    
    private func validate(_ state: inout ReserveState) {
        state.validationState = .valid
        guard state.coinAmount.isNotEmpty else {
            return setupState(with: &state, errorString: L.CreateWallet.Form.Error.allFieldsRequired)
        }
        
        guard let amount = state.coinAmount.decimalValue,
              isValidXRPAmount(amount: amount, state: state) else {
            return setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.invalidAmount)
        }
        
        guard amount > 0 else {
            return setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooLowAmount)
        }
        
        if state.coin?.type != .catm, let fee = state.coinDetails?.txFee {
            guard amount.greaterThanOrEqualTo(fee) else {
                return setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.lessThanFee)
            }
        }
        
        guard amount.lessThanOrEqualTo(state.maxValue) else {
            return setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooHighAmount)
        }
        
        if state.coin?.type.isETHBased ?? false, let fee = state.coinDetails?.txFee {
            let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
            
            if !ethBalance.greaterThanOrEqualTo(fee) && amount.greaterThanOrEqualTo(state.coinBalance?.balance ?? 0) {
                return setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.insufficientETHBalance)
            }
        }
    }
    
    private func setupState(with state: inout ReserveState, errorString: String) {
        let errorString = localize(errorString)
        state.coinAmountError = errorString
        state.validationState = .invalid(errorString)
    }
    
    private func isValidXRPAmount(amount: Decimal, state: ReserveState) -> Bool {
        if state.coin?.type == .ripple {
            return (state.coinBalance?.balance ?? 0 - amount).greaterThanOrEqualTo(20)
        }
        return true
    }
}
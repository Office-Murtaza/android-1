import Foundation

enum RecallAction: Equatable {
    case setupCoin(BTMCoin)
    case setupCoinBalances([CoinBalance]?, CustomCoinType)
    case setupTransactionDetails(TransactionDetails)
    case setupCoinDetails(CoinDetails?)
    case updateCurrencyAmount(String?)
    case updateCoinAmount(String?)
    case updateValidationState
    case makeInvalidState(String)
}

struct RecallState: Equatable {
    var coin: BTMCoin?
    var coinBalances: [CoinBalance]?
    var coinBalance: CoinBalance?
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
    
    var reservedBalance: Decimal {
        return coinBalance?.reservedBalance ?? 0
    }
    
    var fee: Decimal {
        return coinDetails?.convertedTxFee ?? coinDetails?.txFee ?? 0
    }
    
    var maxValue: Decimal {
        return max(0, reservedBalance - fee)
    }
    
    var isFieldNotEmpty: Bool {
        return coinAmount.isNotEmpty
    }
}

final class RecallStore: ViewStore<RecallAction, RecallState> {
    override var initialState: RecallState {
        return RecallState()
    }
    
    override func reduce(state: RecallState, action: RecallAction) -> RecallState {
        var state = state
        
        switch action {
        case let .setupCoin(coin): state.coin = coin
        case let .setupCoinBalances(coinBalances, coinType):
            state.coinBalances = coinBalances
            state.coinBalance = coinBalances?.first { $0.type == coinType }
        case let .setupCoinDetails(coinDetails): state.coinDetails = coinDetails
        case let .setupTransactionDetails(transactionDetails): state.transactionDetails = transactionDetails
        case let .updateCurrencyAmount(amount):
            let currencyAmount = (amount ?? "").fiatWithdrawFormatted
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
    
    private func validate(_ state: inout RecallState) {
        state.validationState = .valid
        
        guard state.coinAmount.isNotEmpty else {
            return setupState(with: &state, errorString: L.CreateWallet.Form.Error.allFieldsRequired)
        }
        
        guard let amount = state.coinAmount.decimalValue else {
            return setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.invalidAmount)
        }
        
        if state.coin?.type != .catm, let fee = state.coinDetails?.txFee {
            guard amount.greaterThanOrEqualTo(fee) else {
                return setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.lessThanFee)
            }
        }
        
        guard state.reservedBalance > state.fee else {
            return setupState(with: &state, errorString: L.Recall.Form.Error.tooLowAmount)
        }
        
        guard amount.lessThanOrEqualTo(state.maxValue) else {
            return setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooHighAmount)
        }
        
        if state.coin?.type.isETHBased == true, let fee = state.coinDetails?.txFee {
            let ethPrice = state.coinBalances?.first { $0.type == .ethereum }?.price ?? 0
            let catmPrice = state.coinBalances?.first { $0.type.isETHBased }?.price ?? 0
            let catmFee = (fee * ethPrice) / catmPrice
            
            if amount.lessThanOrEqualTo(catmFee) {
                return setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.insufficientETHBalance)
            }
        }
    }
    
    private func setupState(with state: inout RecallState, errorString: String) {
        let errorString = localize(errorString)
        state.coinAmountError = errorString
        state.validationState = .invalid(errorString)
    }
}

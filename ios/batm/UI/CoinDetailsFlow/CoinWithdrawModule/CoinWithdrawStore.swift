import Foundation

enum CoinWithdrawAction: Equatable {
    case setupCoin(BTMCoin)
    case setupCoinBalances([CoinBalance])
    case setupCoinDetails(CoinDetails)
    case updateAddress(String?)
    case updateCoinAmount(String?)
    case updateAddressError(String?)
    case updateCoinAmountError(String?)
    case updateValidationState
}

struct CoinWithdrawState: Equatable {
    
    var coin: BTMCoin?
    var coinBalances: [CoinBalance]?
    var coinDetails: CoinDetails?
    var address: String = ""
    var coinAmount: String = ""
    var addressError: String?
    var coinAmountError: String?
    var validationState: ValidationState = .unknown
    
    var coinBalance: CoinBalance? {
        return coinBalances?.first { $0.type == coin?.type }
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
    
    var fiatAmount: String {
        let coinAmountDecimal = coinAmount.decimalValue ?? 0
        let price = coinBalance?.price ?? 0
        
        return (coinAmountDecimal * price).fiatFormatted.withDollarSign
    }
    
    var isAllFieldsNotEmpty: Bool {
        return address.count > 0 && coinAmount.count > 0
    }
    
}

final class CoinWithdrawStore: ViewStore<CoinWithdrawAction, CoinWithdrawState> {
    
    override var initialState: CoinWithdrawState {
        return CoinWithdrawState()
    }
    
    override func reduce(state: CoinWithdrawState, action: CoinWithdrawAction) -> CoinWithdrawState {
        var state = state
        
        switch action {
        case let .setupCoin(coin): state.coin = coin
        case let .setupCoinBalances(coinBalances): state.coinBalances = coinBalances
        case let .setupCoinDetails(coinDetails): state.coinDetails = coinDetails
        case let .updateAddress(address):
            state.address = address ?? ""
            state.addressError = nil
        case let .updateCoinAmount(amount):
            if let amount = amount, amount.isFirstCharacterDigit {
                state.coinAmount = amount.coinWithdrawFormatted
            } else {
                state.coinAmount = ""
            }
            state.coinAmountError = nil
        case let .updateAddressError(addressError): state.addressError = addressError
        case let .updateCoinAmountError(coinAmountError): state.coinAmountError = coinAmountError
        case .updateValidationState: validate(&state)
        }
        
        return state
    }
    
    private func validate(_ state: inout CoinWithdrawState) {
        state.validationState = .valid
        
        if state.address.count == 0 {
            let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
            state.addressError = errorString
            state.validationState = .invalid(errorString)
        } else if let coin = state.coin, coin.type.defaultCoinType.validate(address: state.address) {
            state.addressError = nil
        } else {
            let errorString = localize(L.CoinWithdraw.Form.Error.invalidAddress)
            state.addressError = errorString
            state.validationState = .invalid(errorString)
        }
        
        if state.coinAmount.count == 0 {
            setupState(with: &state, errorString: L.CreateWallet.Form.Error.fieldRequired)
        } else if state.coinAmount.decimalValue == nil {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.invalidAmount)
        } else if state.coinAmount.decimalValue?.lessThan(state.coinDetails?.txFee ?? 0) == true {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.lessThanFee)
        } else if state.coinAmount.decimalValue! <= 0 {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooLowAmount)
        } else if !state.coinAmount.decimalValue!.lessThanOrEqualTo(state.maxValue) {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooHighAmount)
        } else {
            state.coinAmountError = nil
            
            if state.coin?.type == .catm, let fee = state.coinDetails?.txFee {
                let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
                
                if !ethBalance.greaterThanOrEqualTo(fee) {
                    setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.insufficientETHBalance)
                }
            }
        }
    }
    
    private func setupState(with state: inout CoinWithdrawState, errorString: String) {
        let errorString = localize(errorString)
        state.coinAmountError = errorString
        state.validationState = .invalid(errorString)
    }
}

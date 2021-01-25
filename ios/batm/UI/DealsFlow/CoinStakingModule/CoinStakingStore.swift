import Foundation

enum CoinStakingAction: Equatable {
    case setupCoin(BTMCoin)
    case setupCoinBalances([CoinBalance])
    case setupCoinDetails(CoinDetails)
    case setupStakeDetails(StakeDetails)
    case updateCoinAmount(String?)
    case updateCoinAmountError(String?)
    case updateValidationState
}

struct CoinStakingState: Equatable {
    var coin: BTMCoin?
    var coinBalances: [CoinBalance]?
    var coinDetails: CoinDetails?
    var stakeDetails: StakeDetails?
    var coinAmount: String = ""
    var coinAmountError: String?
    var validationState: ValidationState = .unknown
    
    var coinBalance: CoinBalance? {
        return coinBalances?.first { $0.type == coin?.type }
    }
    
    var fiatAmount: String {
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
    
    var isAllFieldsNotEmpty: Bool {
        return coinAmount.count > 0
    }
    
    var shouldShowCreateButton: Bool {
        return stakeDetails?.status == .notExist || stakeDetails?.status == .withdrawn
    }
    
    var shouldShowCancelButton: Bool {
        return stakeDetails?.status == .created
    }
    
    var shouldShowWithdrawButton: Bool {
        return stakeDetails?.status == .canceled
    }
    
    var shouldWithdrawButtonEnabled: Bool {
        return stakeDetails?.status == .canceled && stakeDetails?.tillWithdrawal == 0
    }
}

final class CoinStakingStore: ViewStore<CoinStakingAction, CoinStakingState> {
    override var initialState: CoinStakingState {
        return CoinStakingState()
    }
    
    override func reduce(state: CoinStakingState, action: CoinStakingAction) -> CoinStakingState {
        var state = state
        
        switch action {
        case let .setupCoin(coin): state.coin = coin
        case let .setupCoinBalances(coinBalances): state.coinBalances = coinBalances
        case let .setupCoinDetails(coinDetails): state.coinDetails = coinDetails
        case let .setupStakeDetails(stakeDetails): state.stakeDetails = stakeDetails
        case let .updateCoinAmount(amount):
            state.coinAmount = (amount ?? "").coinWithdrawFormatted
            state.coinAmountError = nil
        case let .updateCoinAmountError(coinAmountError): state.coinAmountError = coinAmountError
        case .updateValidationState: validate(&state)
        }
        
        return state
    }
    
    private func validateETHBalance(_ state: inout CoinStakingState) {
        guard state.coin?.type.isETHBased ?? false, let fee = state.coinDetails?.txFee else { return }
        
        let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
        
        if !ethBalance.greaterThanOrEqualTo(fee) {
            let errorString = localize(L.CoinWithdraw.Form.Error.insufficientETHBalance)
            state.coinAmountError = errorString
            state.validationState = .invalid(errorString)
        }
    }
    
    private func validate(_ state: inout CoinStakingState) {
        state.validationState = .valid
        
        state.coinAmountError = nil
        
        guard let status = state.stakeDetails?.status else {
            state.coinAmountError = ""
            state.validationState = .invalid("")
            return
        }
        
        if status == .notExist || status == .withdrawn {
            validateETHBalance(&state)
            return
        }
        
        if state.coinAmount.count == 0 {
            let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
            state.coinAmountError = errorString
            state.validationState = .invalid(errorString)
        } else if state.coinAmount.decimalValue == nil {
            let errorString = localize(L.CoinWithdraw.Form.Error.invalidAmount)
            state.coinAmountError = errorString
            state.validationState = .invalid(errorString)
        } else if state.coinAmount.decimalValue! <= 0 {
            let errorString = localize(L.CoinWithdraw.Form.Error.tooLowAmount)
            state.coinAmountError = errorString
            state.validationState = .invalid(errorString)
        } else if !state.coinAmount.decimalValue!.lessThanOrEqualTo(state.maxValue) {
            let errorString = localize(L.CoinWithdraw.Form.Error.tooHighAmount)
            state.coinAmountError = errorString
            state.validationState = .invalid(errorString)
        } else {
            validateETHBalance(&state)
        }
    }
}
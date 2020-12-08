import Foundation

enum ReserveAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalances([CoinBalance])
  case setupCoinDetails(CoinDetails)
  case updateCurrencyAmount(String?)
  case updateCoinAmount(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct ReserveState: Equatable {
  
  var coin: BTMCoin?
  var coinBalances: [CoinBalance]?
  var coinDetails: CoinDetails?
  var currencyAmount: String = ""
  var coinAmount: String = ""
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
    case let .setupCoinBalances(coinBalances): state.coinBalances = coinBalances
    case let .setupCoinDetails(coinDetails): state.coinDetails = coinDetails
    case let .updateCurrencyAmount(amount):
      let currencyAmount = (amount ?? "").fiatWithdrawFormatted
      let decimalCurrencyAmount = currencyAmount.decimalValue
      let price = state.coinBalance!.price
      let coinAmount = decimalCurrencyAmount == nil ? "" : (decimalCurrencyAmount! / price).coinFormatted
      
      state.coinAmount = coinAmount
      state.currencyAmount = currencyAmount
    case let .updateCoinAmount(amount):
      let coinAmount = (amount ?? "").coinWithdrawFormatted
      let decimalCoinAmount = coinAmount.decimalValue
      let price = state.coinBalance!.price
      let currencyAmount = decimalCoinAmount == nil ? "" : (decimalCoinAmount! * price).fiatFormatted
      
      state.coinAmount = coinAmount
      state.currencyAmount = currencyAmount
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: ReserveState) -> ValidationState {
    guard state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let amount = state.coinAmount.decimalValue,
          isValidXRPAmount(amount: amount, state: state) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard amount > 0 else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooLowAmount))
    }
    
    if state.coin?.type != .catm, let fee = state.coinDetails?.txFee {
        guard amount.greaterThanOrEqualTo(fee) else {
            return .invalid(localize(L.CoinWithdraw.Form.Error.lessThanFee))
        }
    }
    
    guard amount.lessThanOrEqualTo(state.maxValue) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    if state.coin?.type.isETHBased ?? false, let fee = state.coinDetails?.txFee {
      let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
      
      if !ethBalance.greaterThanOrEqualTo(fee) && amount.greaterThanOrEqualTo(state.coinBalance?.balance ?? 0) {
        return .invalid(localize(L.CoinWithdraw.Form.Error.insufficientETHBalance))
      }
    }
    
    return .valid
  }
    
    private func isValidXRPAmount(amount: Decimal, state: ReserveState) -> Bool {
        if state.coin?.type == .ripple {
            return amount.greaterThanOrEqualTo(20)
        }
        return true
    }
}

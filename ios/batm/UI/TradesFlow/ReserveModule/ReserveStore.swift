import Foundation

enum ReserveAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalances([CoinBalance])
  case setupCoinSettings(CoinSettings)
  case updateCurrencyAmount(String?)
  case updateCoinAmount(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct ReserveState: Equatable {
  
  var coin: BTMCoin?
  var coinBalances: [CoinBalance]?
  var coinSettings: CoinSettings?
  var currencyAmount: String = ""
  var coinAmount: String = ""
  var validationState: ValidationState = .unknown
  
  var coinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == coin?.type }
  }
  
  var maxValue: Double {
    guard let type = coin?.type, let balance = coinBalance?.balance, let fee = coinSettings?.txFee else { return 0 }
    
    if type == .catm {
      return balance
    }
    
    return max(0, balance - fee)
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
    case let .setupCoinSettings(coinSettings): state.coinSettings = coinSettings
    case let .updateCurrencyAmount(amount):
      let currencyAmount = (amount ?? "").fiatWithdrawFormatted
      let doubleCurrencyAmount = currencyAmount.doubleValue
      let price = state.coinBalance!.price
      let coinAmount = doubleCurrencyAmount == nil ? "" : (doubleCurrencyAmount! / price).coinFormatted
      
      state.coinAmount = coinAmount
      state.currencyAmount = currencyAmount
    case let .updateCoinAmount(amount):
      let coinAmount = (amount ?? "").coinWithdrawFormatted
      let doubleCoinAmount = coinAmount.doubleValue
      let price = state.coinBalance!.price
      let currencyAmount = doubleCoinAmount == nil ? "" : (doubleCoinAmount! * price).fiatFormatted
      
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
    
    guard let amount = state.coinAmount.doubleValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard amount > 0 else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooLowAmount))
    }
    
    guard amount.lessThanOrEqualTo(state.maxValue) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    if state.coin?.type == .catm, let fee = state.coinSettings?.txFee {
      let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
      
      if !ethBalance.greaterThanOrEqualTo(fee) {
        return .invalid(localize(L.CoinWithdraw.Form.Error.insufficientETHBalance))
      }
    }
    
    return .valid
  }
}

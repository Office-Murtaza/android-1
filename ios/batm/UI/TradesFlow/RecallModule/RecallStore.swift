import Foundation

enum RecallAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalances([CoinBalance])
  case setupCoinSettings(CoinSettings)
  case updateCurrencyAmount(String?)
  case updateCoinAmount(String?)
  case updateCode(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct RecallState: Equatable {
  
  var coin: BTMCoin?
  var coinBalances: [CoinBalance]?
  var coinSettings: CoinSettings?
  var currencyAmount: String = ""
  var coinAmount: String = ""
  var code: String = ""
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
  var coinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == coin?.type }
  }
  
  var reservedBalance: Double {
    return coinBalance?.reservedBalance ?? 0
  }
  
  var fee: Double {
    return coinSettings?.recallFee ?? coinSettings?.txFee ?? 0
  }
  
  var maxValue: Double {
    return max(0, reservedBalance - fee)
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
    case let .updateCode(code): state.code = code ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    case .showCodePopup: state.shouldShowCodePopup = true
    }
    
    return state
  }
  
  private func validate(_ state: RecallState) -> ValidationState {
    guard state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let amount = state.coinAmount.doubleValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard state.reservedBalance > state.fee else {
      return .invalid(localize(L.Recall.Form.Error.tooLowAmount))
    }
    
    guard amount.lessThanOrEqualTo(state.maxValue) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    guard !state.shouldShowCodePopup || state.code.count == 4 else {
      return .invalid(localize(L.CreateWallet.Code.Error.title))
    }
    
    return .valid
  }
}

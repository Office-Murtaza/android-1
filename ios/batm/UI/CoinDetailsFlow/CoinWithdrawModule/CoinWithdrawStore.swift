import Foundation

enum CoinWithdrawAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalance(CoinBalance)
  case updateAddress(String?)
  case updateCurrencyAmount(String?)
  case updateCoinAmount(String?)
  case updateCode(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct CoinWithdrawState: Equatable {
  
  var coin: BTMCoin?
  var coinBalance: CoinBalance?
  var address: String = ""
  var currencyAmount: String = ""
  var coinAmount: String = ""
  var code: String = ""
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
  var maxValue: Double {
    return coinBalance?.maxValue ?? 0
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
    case let .setupCoinBalance(coinBalance): state.coinBalance = coinBalance
    case let .updateAddress(address): state.address = address ?? ""
    case let .updateCurrencyAmount(amount):
      let currencyAmount = (amount ?? "").fiatFormatted
      let doubleCurrencyAmount = Double(currencyAmount)
      let price = state.coinBalance!.price
      let coinAmount = doubleCurrencyAmount == nil ? "" : (doubleCurrencyAmount! / price).coinFormatted
      
      state.coinAmount = coinAmount
      state.currencyAmount = currencyAmount
    case let .updateCoinAmount(amount):
      let coinAmount = (amount ?? "").coinFormatted
      let doubleCoinAmount = Double(coinAmount)
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
  
  private func validate(_ state: CoinWithdrawState) -> ValidationState {
    guard state.address.count > 0, state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let coin = state.coin, coin.type.validate(address: state.address) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAddress))
    }
    
    guard let amount = Double(state.coinAmount) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard amount > coin.type.fee else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooLowAmount))
    }
    
    guard amount <= state.maxValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    guard !state.shouldShowCodePopup || state.code.count == 4 else {
      return .invalid(localize(L.CreateWallet.Code.Error.title))
    }
    
    return .valid
  }
}

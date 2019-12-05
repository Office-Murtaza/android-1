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
    guard let balance = coinBalance?.balance, let fee = coin?.feeInCoin, balance > fee else { return 0 }
    return balance - fee
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
  
  private func validate(_ state: CoinWithdrawState) -> ValidationState {
    guard state.address.count > 0, state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let coin = state.coin, coin.type.validate(address: state.address) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAddress))
    }
    
    guard let amount = state.coinAmount.doubleValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard amount > 0 else {
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

import Foundation

enum CoinSellAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalance(CoinBalance)
  case setupCoinSettings(CoinSettings)
  case setupDetails(SellDetails)
  case setupPreSubmitResponse(PreSubmitResponse)
  case updateFromAnotherAddress(Bool)
  case updateCurrencyAmount(String?)
  case makeMaxCurrencyAmount
  case updateCode(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct CoinSellState: Equatable {
  
  var coin: BTMCoin?
  var coinBalance: CoinBalance?
  var coinSettings: CoinSettings?
  var details: SellDetails?
  var presubmitResponse: PreSubmitResponse?
  var fromAnotherAddress: Bool = false
  var currencyAmount: String = ""
  var code: String = ""
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
  var maxCurrencyLimit: Double {
    guard
      let dailyLimit = details?.dailyLimit,
      let transactionLimit = details?.transactionLimit
    else { return 0 }
    
    return min(dailyLimit, transactionLimit)
  }
  
  var maxCurrencyValue: Double {
    if fromAnotherAddress { return maxCurrencyLimit.nearestNumberThatCanBeGivenByTwentyAndFifty }
    
    guard
      let balance = coinBalance?.balance,
      let fee = coinSettings?.txFee,
      balance > fee,
      let price = coinBalance?.price,
      let profitRate = details?.profitRate
    else { return 0 }
    let balanceMaxValue = balance - fee
    let potentialMaxCurrencyValue = balanceMaxValue * price / profitRate
    let maxCurrencyValue = min(potentialMaxCurrencyValue, maxCurrencyLimit)
    
    return maxCurrencyValue.nearestNumberThatCanBeGivenByTwentyAndFifty
  }
  
  var maxValue: Double {
    guard
      let price = coinBalance?.price,
      let profitRate = details?.profitRate
    else { return 0 }
    
    return (maxCurrencyValue / price * profitRate)
  }
  
  var coinAmount: String {
    guard
      let price = coinBalance?.price,
      let profitRate = details?.profitRate,
      let currencyAmount = currencyAmount.doubleValue
    else { return "" }
    
    let coinAmount = currencyAmount / price * profitRate
    return coinAmount.coinFormatted
  }
  
  var isValidIfResponseExists: Bool {
    guard let response = presubmitResponse, !fromAnotherAddress else { return true }
    
    guard let balance = coinBalance?.balance, let fee = coinSettings?.txFee, balance > fee else { return false }
    
    return (balance - fee).greaterThanOrEqualTo(response.amount)
  }
  
}

final class CoinSellStore: ViewStore<CoinSellAction, CoinSellState> {
  
  override var initialState: CoinSellState {
    return CoinSellState()
  }
  
  override func reduce(state: CoinSellState, action: CoinSellAction) -> CoinSellState {
    var state = state
    
    switch action {
    case let .setupCoin(coin): state.coin = coin
    case let .setupCoinBalance(coinBalance): state.coinBalance = coinBalance
    case let .setupCoinSettings(coinSettings): state.coinSettings = coinSettings
    case let .setupDetails(details): state.details = details
    case let .setupPreSubmitResponse(response): state.presubmitResponse = response
    case let .updateFromAnotherAddress(fromAnotherAddress): state.fromAnotherAddress = fromAnotherAddress
    case let .updateCurrencyAmount(amount): state.currencyAmount = (amount ?? "").fiatSellFormatted
    case .makeMaxCurrencyAmount: state.currencyAmount = state.maxCurrencyValue.fiatSellFormatted
    case let .updateCode(code): state.code = code ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    case .showCodePopup: state.shouldShowCodePopup = true
    }
    
    return state
  }
  
  private func validate(_ state: CoinSellState) -> ValidationState {
    guard state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let currencyAmount = state.currencyAmount.doubleValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard Int(currencyAmount.nearestNumberThatCanBeGivenByTwentyAndFifty) == Int(currencyAmount) else {
      return .invalid(localize(L.CoinSell.Form.Error.notMultiple))
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
    
    guard !state.shouldShowCodePopup || state.code.count == 4 else {
      return .invalid(localize(L.CreateWallet.Code.Error.title))
    }
    
    if !state.isValidIfResponseExists {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    return .valid
  }
}

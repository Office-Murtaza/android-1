import Foundation

enum CoinSellAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalance(CoinBalance)
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
    if fromAnotherAddress { return maxCurrencyLimit.multipleOfTwentyOrFifty }
    
    guard
      let price = coinBalance?.price,
      let balanceMaxValue = coinBalance?.maxValue,
      let profitRate = details?.profitRate
    else { return 0 }
    
    let potentialMaxCurrencyValue = balanceMaxValue * price / profitRate
    let maxCurrencyValue = min(potentialMaxCurrencyValue, maxCurrencyLimit)
    
    return maxCurrencyValue.multipleOfTwentyOrFifty
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
      let currencyAmount = Double(currencyAmount)
    else { return "" }
    
    let coinAmount = currencyAmount / price * profitRate
    return coinAmount.coinFormatted
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
    guard let coin = state.coin, state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let currencyAmount = Double(state.currencyAmount) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard Int(currencyAmount.multipleOfTwentyOrFifty) == Int(currencyAmount) else {
      return .invalid(localize(L.CoinSell.Form.Error.notMultiple))
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
    
    if let response = state.presubmitResponse, let coinBalance = state.coinBalance,
      !state.fromAnotherAddress && response.amount > coinBalance.maxValue {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    return .valid
  }
}

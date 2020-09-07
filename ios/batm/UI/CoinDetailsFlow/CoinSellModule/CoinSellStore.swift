import Foundation

enum CoinSellAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalances([CoinBalance])
  case setupCoinSettings(CoinSettings)
  case setupDetails(SellDetails)
  case setupPreSubmitResponse(PreSubmitResponse)
  case updateFromAnotherAddress(Bool)
  case updateCurrencyAmount(String?)
  case makeMaxCurrencyAmount
  case updateValidationState
  case makeInvalidState(String)
}

struct CoinSellState: Equatable {
  
  var coin: BTMCoin?
  var coinBalances: [CoinBalance]?
  var coinSettings: CoinSettings?
  var details: SellDetails?
  var presubmitResponse: PreSubmitResponse?
  var fromAnotherAddress: Bool = false
  var currencyAmount: String = ""
  var validationState: ValidationState = .unknown
  
  var coinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == coin?.type }
  }
  
  var maxCurrencyLimit: Decimal {
    guard
      let dailyLimit = details?.dailyLimit,
      let transactionLimit = details?.transactionLimit
    else { return 0 }
    
    return min(dailyLimit, transactionLimit)
  }
  
  var maxCurrencyValue: Decimal {
    if fromAnotherAddress { return maxCurrencyLimit.nearestNumberThatCanBeGivenByTwentyAndFifty }
    
    guard
      let type = coin?.type,
      let balance = coinBalance?.balance,
      let fee = coinSettings?.txFee,
      let price = coinBalance?.price,
      let profitRate = details?.profitRate
    else { return 0 }
    let balanceMaxValue: Decimal
    
    if type == .catm {
      balanceMaxValue = balance
    } else {
      balanceMaxValue = max(0, balance - fee)
    }
    
    let potentialMaxCurrencyValue = balanceMaxValue * price / profitRate
    let maxCurrencyValue = min(potentialMaxCurrencyValue, maxCurrencyLimit)
    
    return maxCurrencyValue.nearestNumberThatCanBeGivenByTwentyAndFifty
  }
  
  var maxValue: Decimal {
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
      let currencyAmount = currencyAmount.decimalValue
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
    case let .setupCoinBalances(coinBalances): state.coinBalances = coinBalances
    case let .setupCoinSettings(coinSettings): state.coinSettings = coinSettings
    case let .setupDetails(details): state.details = details
    case let .setupPreSubmitResponse(response): state.presubmitResponse = response
    case let .updateFromAnotherAddress(fromAnotherAddress): state.fromAnotherAddress = fromAnotherAddress
    case let .updateCurrencyAmount(amount): state.currencyAmount = (amount ?? "").fiatSellFormatted
    case .makeMaxCurrencyAmount: state.currencyAmount = state.maxCurrencyValue.fiatSellFormatted
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: CoinSellState) -> ValidationState {
    guard state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let currencyAmount = state.currencyAmount.decimalValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard currencyAmount.nearestNumberThatCanBeGivenByTwentyAndFifty.intValue == currencyAmount.intValue else {
      return .invalid(localize(L.CoinSell.Form.Error.notMultiple))
    }
    
    guard let amount = state.coinAmount.decimalValue else {
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
    
    if !state.isValidIfResponseExists {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    return .valid
  }
}

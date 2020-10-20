import Foundation

enum RecallAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalances([CoinBalance])
  case setupCoinDetails(CoinDetails)
  case updateCurrencyAmount(String?)
  case updateCoinAmount(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct RecallState: Equatable {
  
  var coin: BTMCoin?
  var coinBalances: [CoinBalance]?
  var coinDetails: CoinDetails?
  var currencyAmount: String = ""
  var coinAmount: String = ""
  var validationState: ValidationState = .unknown
  
  var coinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == coin?.type }
  }
  
  var reservedBalance: Decimal {
    return coinBalance?.reservedBalance ?? 0
  }
  
  var fee: Decimal {
    return coinDetails?.recallFee ?? coinDetails?.txFee ?? 0
  }
  
  var maxValue: Decimal {
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
  
  private func validate(_ state: RecallState) -> ValidationState {
    guard state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let amount = state.coinAmount.decimalValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard state.reservedBalance > state.fee else {
      return .invalid(localize(L.Recall.Form.Error.tooLowAmount))
    }
    
    guard amount.lessThanOrEqualTo(state.maxValue) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    return .valid
  }
}

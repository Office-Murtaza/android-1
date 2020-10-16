import Foundation
import TrustWalletCore

enum ExchangePickerOption {
  case coins
  case none
}

enum CoinExchangeAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalances([CoinBalance])
  case setupCoinSettings(CoinSettings)
  case updateFromCoinAmount(String?)
  case updateToCoinType(CustomCoinType)
  case updateFromCoinAmountError(String?)
  case updateToCoinTypeError(String?)
  case updateValidationState
}

struct CoinExchangeState: Equatable {
  
  var fromCoin: BTMCoin?
  var toCoinType: CustomCoinType?
  var coinBalances: [CoinBalance]?
  var coinSettings: CoinSettings?
  var fromCoinAmount: String = ""
  var fromCoinAmountError: String?
  var toCoinTypeError: String?
  var validationState: ValidationState = .unknown
  
  var fromCoinFiatAmount: String {
    let fromCoinAmountDecimal = fromCoinAmount.decimalValue ?? 0
    let price = fromCoinBalance?.price ?? 0
    
    return (fromCoinAmountDecimal * price).fiatFormatted.withDollarSign
  }
  
  var toCoinAmount: String {
    guard let toCoinType = toCoinType else { return "" }
    
    guard
      let fromCoinAmountDecimal = fromCoinAmount.decimalValue,
      let fromCoinPrice = fromCoinBalance?.price, let toCoinPrice = toCoinBalance?.price,
      let profitExchange = coinSettings?.profitExchange
    else {
      return 0.0.coinFormatted.withCoinType(toCoinType)
    }
    let toCoinAmountDecimal = fromCoinAmountDecimal * fromCoinPrice / toCoinPrice * (100 - profitExchange) / 100
    return toCoinAmountDecimal.coinFormatted(fractionDigits: coinSettings?.scale).withCoinType(toCoinType)
  }
  
  var maxValue: Decimal {
    guard let type = fromCoin?.type, let balance = fromCoinBalance?.balance, let fee = coinSettings?.txFee else { return 0 }
    
    switch type {
    case .catm:
      return balance
    case .ripple:
      return max(0, balance - fee - 20)
    default:
      return max(0, balance - fee)
    }
  }
  
  var fromCoinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == fromCoin?.type }
  }
  
  var toCoinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == toCoinType }
  }
  
  var otherCoinBalances: [CoinBalance]? {
    return coinBalances?.filter { $0.type != fromCoin?.type }
  }
  
  var isAllFieldsNotEmpty: Bool {
    return fromCoinAmount.count > 0 && toCoinType != nil
  }
  
}

final class CoinExchangeStore: ViewStore<CoinExchangeAction, CoinExchangeState> {
  
  override var initialState: CoinExchangeState {
    return CoinExchangeState()
  }
  
  override func reduce(state: CoinExchangeState, action: CoinExchangeAction) -> CoinExchangeState {
    var state = state
    
    switch action {
    case let .setupCoin(coin):
      state.fromCoin = coin
      state.toCoinType = state.coinBalances?.first(where: { $0.type != coin.type })?.type
    case let .setupCoinBalances(coinBalances):
      state.coinBalances = coinBalances
      state.toCoinType = coinBalances.first(where: { $0.type != state.fromCoin?.type })?.type
    case let .setupCoinSettings(coinSettings): state.coinSettings = coinSettings
    case let .updateFromCoinAmount(amount):
      state.fromCoinAmount = (amount ?? "").coinWithdrawFormatted
      state.fromCoinAmountError = nil
    case let .updateToCoinType(coinType):
      state.toCoinType = coinType
      state.toCoinTypeError = nil
    case let .updateFromCoinAmountError(fromCoinAmountError): state.fromCoinAmountError = fromCoinAmountError
    case let .updateToCoinTypeError(toCoinTypeError): state.toCoinTypeError = toCoinTypeError
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout CoinExchangeState) {
    state.validationState = .valid
    
    if state.fromCoinAmount.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.fromCoinAmountError = errorString
      state.validationState = .invalid(errorString)
    } else if state.fromCoinAmount.decimalValue == nil {
      let errorString = localize(L.CoinWithdraw.Form.Error.invalidAmount)
      state.fromCoinAmountError = errorString
      state.validationState = .invalid(errorString)
    } else if state.fromCoinAmount.decimalValue! <= 0 {
      let errorString = localize(L.CoinWithdraw.Form.Error.tooLowAmount)
      state.fromCoinAmountError = errorString
      state.validationState = .invalid(errorString)
    } else if !state.fromCoinAmount.decimalValue!.lessThanOrEqualTo(state.maxValue) {
      let errorString = localize(L.CoinWithdraw.Form.Error.tooHighAmount)
      state.fromCoinAmountError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.fromCoinAmountError = nil
      
      if state.fromCoin?.type == .catm, let fee = state.coinSettings?.txFee {
        let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
        
        if !ethBalance.greaterThanOrEqualTo(fee) {
          let errorString = localize(L.CoinWithdraw.Form.Error.insufficientETHBalance)
          state.fromCoinAmountError = errorString
          state.validationState = .invalid(errorString)
        }
      }
    }
    
    if state.toCoinType == nil {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.toCoinTypeError = errorString
      state.validationState = .invalid(errorString)
    }
  }
}

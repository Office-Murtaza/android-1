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
  case updateCode(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct CoinExchangeState: Equatable {
  
  var fromCoin: BTMCoin?
  var toCoinType: CustomCoinType?
  var coinBalances: [CoinBalance]?
  var coinSettings: CoinSettings?
  var fromCoinAmount: String = ""
  var code: String = ""
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
  var toCoinAmount: String? {
    guard let fromCoinAmountDouble = fromCoinAmount.doubleValue else { return nil }
    guard let fromCoinPrice = fromCoinBalance?.price, let toCoinPrice = toCoinBalance?.price else { return nil }
    guard let profitC2C = coinSettings?.profitC2C else { return nil }
    
    let toCoinAmountDouble = fromCoinAmountDouble * fromCoinPrice / toCoinPrice * (100 - profitC2C) / 100
    return toCoinAmountDouble.coinFormatted
  }
  
  var maxValue: Double {
    guard let type = fromCoin?.type, let balance = fromCoinBalance?.balance, let fee = coinSettings?.txFee else { return 0 }
    
    if type == .catm {
      return balance
    }
    
    return max(0, balance - fee)
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
    case let .updateFromCoinAmount(amount): state.fromCoinAmount = (amount ?? "").coinWithdrawFormatted
    case let .updateToCoinType(coinType): state.toCoinType = coinType
    case let .updateCode(code): state.code = code ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    case .showCodePopup: state.shouldShowCodePopup = true
    }
    
    return state
  }
  
  private func validate(_ state: CoinExchangeState) -> ValidationState {
    guard state.fromCoinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let amount = state.fromCoinAmount.doubleValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard amount > 0 else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooLowAmount))
    }
    
    guard amount.lessThanOrEqualTo(state.maxValue) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    if state.fromCoin?.type == .catm, let fee = state.coinSettings?.txFee {
      let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
      
      if !ethBalance.greaterThanOrEqualTo(fee) {
        return .invalid(localize(L.CoinWithdraw.Form.Error.insufficientETHBalance))
      }
    }
    
    guard !state.shouldShowCodePopup || state.code.count == 4 else {
      return .invalid(localize(L.CreateWallet.Code.Error.title))
    }
    
    return .valid
  }
}

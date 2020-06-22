import Foundation
import TrustWalletCore

struct SubmitTradeData: Equatable {
  var coinType: CustomCoinType
  var tradeType: TradeType
  var payment: String
  var margin: Double
  var minLimit: Int
  var maxLimit: Int
  var terms: String
}

enum CreateEditTradeAction: Equatable {
  case setupCoinBalance(CoinBalance)
  case setupTrade(BuySellTrade)
  case updateSelectedType(TradeType)
  case updatePayment(String?)
  case updateMargin(String?)
  case updateMinLimit(String?)
  case updateMaxLimit(String?)
  case updateTerms(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct CreateEditTradeState: Equatable {
  
  var coinBalance: CoinBalance?
  var trade: BuySellTrade?
  var selectedType: TradeType = .buy
  var payment: String = ""
  var margin: String = ""
  var minLimit: String = ""
  var maxLimit: String = ""
  var terms: String = ""
  var validationState: ValidationState = .unknown
  
  var data: SubmitTradeData? {
    guard let coinType = coinBalance?.type,
      let margin = margin.doubleValue,
      let minLimit = minLimit.intValue,
      let maxLimit = maxLimit.intValue else {
      return nil
    }
    return SubmitTradeData(coinType: coinType,
                               tradeType: selectedType,
                               payment: payment,
                               margin: margin,
                               minLimit: minLimit,
                               maxLimit: maxLimit,
                               terms: terms)
  }
  
}

final class CreateEditTradeStore: ViewStore<CreateEditTradeAction, CreateEditTradeState> {
  
  override var initialState: CreateEditTradeState {
    return CreateEditTradeState()
  }
  
  override func reduce(state: CreateEditTradeState, action: CreateEditTradeAction) -> CreateEditTradeState {
    var state = state
    
    switch action {
    case let .setupCoinBalance(coinBalance): state.coinBalance = coinBalance
    case let .setupTrade(trade): state.trade = trade
    case let .updateSelectedType(type): state.selectedType = type
    case let .updatePayment(payment): state.payment = payment ?? ""
    case let .updateMargin(margin): state.margin = margin ?? ""
    case let .updateMinLimit(minLimit): state.minLimit = minLimit ?? ""
    case let .updateMaxLimit(maxLimit): state.maxLimit = maxLimit ?? ""
    case let .updateTerms(terms): state.terms = terms ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: CreateEditTradeState) -> ValidationState {
    guard state.payment.isNotEmpty,
      state.margin.isNotEmpty,
      state.minLimit.isNotEmpty,
      state.maxLimit.isNotEmpty,
      state.terms.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.payment.count <= 100 else {
      return .invalid(localize(L.CreateEditTrade.Form.Error.tooManyCharactersInPayment))
    }
    
    guard let margin = state.margin.doubleValue, margin > 0, margin <= 100 else {
      return .invalid(localize(L.CreateEditTrade.Form.Error.invalidMargin))
    }
    
    guard let minLimit = state.minLimit.doubleValue, minLimit > 0 else {
      return .invalid(localize(L.CreateEditTrade.Form.Error.invalidMinLimit))
    }
    
    guard let maxLimit = state.maxLimit.doubleValue, maxLimit.greaterThanOrEqualTo(minLimit) else {
      return .invalid(localize(L.CreateEditTrade.Form.Error.invalidMaxLimit))
    }
    
    guard state.terms.count <= 255 else {
      return .invalid(localize(L.CreateEditTrade.Form.Error.tooManyCharactersInTerms))
    }
    
    return .valid
  }
}

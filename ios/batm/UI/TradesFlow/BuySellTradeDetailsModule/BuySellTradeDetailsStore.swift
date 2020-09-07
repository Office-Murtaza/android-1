import Foundation
import TrustWalletCore

struct SubmitTradeRequestData {
  var coinType: CustomCoinType
  var trade: BuySellTrade
  var coinAmount: Decimal
  var currencyAmount: Decimal
  var details: String
}

enum BuySellTradeDetailsAction: Equatable {
  case setupCoinBalance(CoinBalance)
  case setupTrade(BuySellTrade)
  case setupType(TradeType)
  case updateCoinAmount(String?)
  case updateCurrencyAmount(String?)
  case updateRequestDetails(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct BuySellTradeDetailsState: Equatable {
  
  var coinBalance: CoinBalance?
  var trade: BuySellTrade?
  var type: TradeType?
  var coinAmount: String = ""
  var currencyAmount: String = ""
  var requestDetails: String = ""
  var validationState: ValidationState = .unknown
  
  var maxValue: Decimal {
    return coinBalance?.reservedBalance ?? 0
  }
  
  var data: SubmitTradeRequestData? {
    guard let coinType = coinBalance?.type,
      let trade = trade,
      let coinAmount = coinAmount.decimalValue,
      let currencyAmount = currencyAmount.decimalValue else {
      return nil
    }
    
    return SubmitTradeRequestData(coinType: coinType,
                                  trade: trade,
                                  coinAmount: coinAmount,
                                  currencyAmount: currencyAmount,
                                  details: requestDetails)
  }
  
}

final class BuySellTradeDetailsStore: ViewStore<BuySellTradeDetailsAction, BuySellTradeDetailsState> {
  
  override var initialState: BuySellTradeDetailsState {
    return BuySellTradeDetailsState()
  }
  
  override func reduce(state: BuySellTradeDetailsState, action: BuySellTradeDetailsAction) -> BuySellTradeDetailsState {
    var state = state
    
    switch action {
    case let .setupCoinBalance(coinBalance): state.coinBalance = coinBalance
    case let .setupTrade(trade): state.trade = trade
    case let .setupType(type): state.type = type
    case let .updateCurrencyAmount(amount):
      let currencyAmount = (amount ?? "").fiatWithdrawFormatted
      let decimalCurrencyAmount = currencyAmount.decimalValue
      let price = state.trade!.price
      let coinAmount = decimalCurrencyAmount == nil ? "" : (decimalCurrencyAmount! / price).coinFormatted
      
      state.coinAmount = coinAmount
      state.currencyAmount = currencyAmount
    case let .updateCoinAmount(amount):
      let coinAmount = (amount ?? "").coinWithdrawFormatted
      let decimalCoinAmount = coinAmount.decimalValue
      let price = state.trade!.price
      let currencyAmount = decimalCoinAmount == nil ? "" : (decimalCoinAmount! * price).fiatFormatted
      
      state.coinAmount = coinAmount
      state.currencyAmount = currencyAmount
    case let .updateRequestDetails(requestDetails): state.requestDetails = requestDetails ?? ""
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: BuySellTradeDetailsState) -> ValidationState {
    guard state.coinAmount.isNotEmpty, state.currencyAmount.isNotEmpty, state.requestDetails.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let coinAmount = state.coinAmount.decimalValue, let currencyAmount = state.currencyAmount.decimalValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard coinAmount > 0, currencyAmount > 0 else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooLowAmount))
    }
    
    guard coinAmount.lessThanOrEqualTo(state.maxValue) else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
    }
    
    guard let trade = state.trade, currencyAmount.greaterThanOrEqualTo(Decimal(trade.minLimit)), currencyAmount.lessThanOrEqualTo(Decimal(trade.maxLimit)) else {
      return .invalid(localize(L.BuySellTradeDetails.Form.Error.notWithinLimits))
    }
    
    guard state.requestDetails.count <= 255 else {
      return .invalid(localize(L.BuySellTradeDetails.Form.Error.tooManyCharacters))
    }
    
    return .valid
  }
}

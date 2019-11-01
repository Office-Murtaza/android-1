import Foundation
import PhoneNumberKit

enum CoinSendGiftAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalance(CoinBalance)
  case updatePhone(String?)
  case pastePhone(String)
  case updateCurrencyAmount(String?)
  case updateCoinAmount(String?)
  case updateCode(String?)
  case updateMessage(String?)
  case updateImageId(String?)
  case updateValidationState
  case makeInvalidState(String)
  case showCodePopup
}

struct CoinSendGiftState: Equatable {
  
  var coin: BTMCoin?
  var coinBalance: CoinBalance?
  var phone: String = ""
  var currencyAmount: String = ""
  var coinAmount: String = ""
  var code: String = ""
  var message: String = ""
  var imageId: String?
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
  var maxValue: Double {
    return coinBalance?.maxValue ?? 0
  }
  
  var formattedPhoneNumber: String {
    guard let phoneNumber = try? PhoneNumberKit.default.parse(phone) else { return "" }
    return PhoneNumberKit.default.format(phoneNumber, toType: .e164)
  }
  
}

final class CoinSendGiftStore: ViewStore<CoinSendGiftAction, CoinSendGiftState> {
  
  override var initialState: CoinSendGiftState {
    return CoinSendGiftState()
  }
  
  override func reduce(state: CoinSendGiftState, action: CoinSendGiftAction) -> CoinSendGiftState {
    var state = state
    
    switch action {
    case let .setupCoin(coin): state.coin = coin
    case let .setupCoinBalance(coinBalance): state.coinBalance = coinBalance
    case let .updatePhone(phone): state.phone = phone ?? ""
    case let .pastePhone(phone):
      if let phoneNumber = try? PhoneNumberKit.default.parse(phone) {
        let phoneNumberString = PhoneNumberKit.default.format(phoneNumber, toType: .national)
        let formattedPhoneNumber = phoneNumberString
          .split { !"0123456789".contains($0) }
          .joined(separator: "-")
        
        state.phone = formattedPhoneNumber
      }
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
    case let .updateMessage(message): state.message = message ?? ""
    case let .updateImageId(imageId): state.imageId = imageId
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    case .showCodePopup: state.shouldShowCodePopup = true
    }
    
    return state
  }
  
  private func validate(_ state: CoinSendGiftState) -> ValidationState {
    guard state.phone.count > 0, state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard let coin = state.coin, let _ = try? PhoneNumberKit.default.parse(state.phone) else {
      return .invalid(localize(L.CoinSendGift.Form.Error.invalidPhone))
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

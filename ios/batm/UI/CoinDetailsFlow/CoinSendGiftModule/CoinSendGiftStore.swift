import Foundation
import PhoneNumberKit

struct ValidatablePhoneNumber: Equatable {
  var phone: String = ""
  var isValid: Bool = false
  var phoneE164 = ""
}

enum CoinSendGiftAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalance(CoinBalance)
  case updatePhone(ValidatablePhoneNumber)
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
  var validatablePhone = ValidatablePhoneNumber()
  var currencyAmount: String = ""
  var coinAmount: String = ""
  var code: String = ""
  var message: String = ""
  var imageId: String?
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
  var maxValue: Double {
    guard let balance = coinBalance?.balance, let fee = coin?.feeInCoin, balance > fee else { return 0 }
    return balance - fee
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
    case let .updatePhone(validatablePhone): state.validatablePhone = validatablePhone
    case let .pastePhone(phone):
      if let parsedPhone = try? PhoneNumberKit.default.parse(phone) {
        let phoneNational = PhoneNumberKit.default.format(parsedPhone, toType: .national)
          .split { !"0123456789".contains($0) }
          .joined()
        let phoneE164 = PhoneNumberKit.default.format(parsedPhone, toType: .e164)
        state.validatablePhone = ValidatablePhoneNumber(phone: phoneNational,
                                                        isValid: true,
                                                        phoneE164: phoneE164)
      }
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
    case let .updateMessage(message): state.message = message ?? ""
    case let .updateImageId(imageId): state.imageId = imageId
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    case .showCodePopup: state.shouldShowCodePopup = true
    }
    
    return state
  }
  
  private func validate(_ state: CoinSendGiftState) -> ValidationState {
    guard state.validatablePhone.phone.count > 0, state.coinAmount.isNotEmpty else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.validatablePhone.isValid else {
      return .invalid(localize(L.CoinSendGift.Form.Error.invalidPhone))
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

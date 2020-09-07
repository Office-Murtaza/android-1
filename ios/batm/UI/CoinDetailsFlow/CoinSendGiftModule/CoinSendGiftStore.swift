import Foundation
import PhoneNumberKit

enum CoinSendGiftAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalances([CoinBalance])
  case setupCoinSettings(CoinSettings)
  case updatePhone(String?)
  case updateCoinAmount(String?)
  case updateMessage(String?)
  case updatePhoneError(String?)
  case updateCoinAmountError(String?)
  case updateMessageError(String?)
  case updateImageId(String?)
  case updateValidationState
}

struct CoinSendGiftState: Equatable {
  
  var coin: BTMCoin?
  var coinBalances: [CoinBalance]?
  var coinSettings: CoinSettings?
  var phone: String = ""
  var coinAmount: String = ""
  var message: String = ""
  var phoneError: String?
  var coinAmountError: String?
  var messageError: String?
  var imageId: String?
  var validationState: ValidationState = .unknown
  
  var coinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == coin?.type }
  }
  
  var maxValue: Decimal {
    guard let type = coin?.type, let balance = coinBalance?.balance, let fee = coinSettings?.txFee else { return 0 }
    
    switch type {
    case .catm:
      return balance
    case .ripple:
      return max(0, balance - fee - 20)
    default:
      return max(0, balance - fee)
    }
  }
  
  var fiatAmount: String {
    let coinAmountDecimal = coinAmount.decimalValue ?? 0
    let price = coinBalance?.price ?? 0
    
    return (coinAmountDecimal * price).fiatFormatted.withDollarSign
  }
  
  var phoneE164: String {
    guard let phoneNumber = try? PhoneNumberKit.default.parse(phone) else { return "" }
    
    return PhoneNumberKit.default.format(phoneNumber, toType: .e164)
  }
  
  var isAllRequiredFieldsNotEmpty: Bool {
    return phoneE164.count > 0 && coinAmount.count > 0
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
    case let .setupCoinBalances(coinBalances): state.coinBalances = coinBalances
    case let .setupCoinSettings(coinSettings): state.coinSettings = coinSettings
    case let .updatePhone(phone):
      state.phone = PartialFormatter.default.formatPartial(phone ?? "")
      state.phoneError = nil
    case let .updateCoinAmount(amount):
      state.coinAmount = (amount ?? "").coinWithdrawFormatted
      state.coinAmountError = nil
    case let .updateMessage(message):
      state.message = message ?? ""
      state.messageError = nil
    case let .updatePhoneError(phoneError): state.phoneError = phoneError
    case let .updateCoinAmountError(coinAmountError): state.coinAmountError = coinAmountError
    case let .updateMessageError(messageError): state.messageError = messageError
    case let .updateImageId(imageId): state.imageId = imageId
    case .updateValidationState: validate(&state)
    }
    
    return state
  }
  
  private func validate(_ state: inout CoinSendGiftState) {
    state.validationState = .valid
    
    if state.phone.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.phoneError = errorString
      state.validationState = .invalid(errorString)
    } else if state.phoneE164.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.notValidPhoneNumber)
      state.phoneError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.phoneError = nil
    }
    
    if state.coinAmount.count == 0 {
      let errorString = localize(L.CreateWallet.Form.Error.fieldRequired)
      state.coinAmountError = errorString
      state.validationState = .invalid(errorString)
    } else if state.coinAmount.decimalValue == nil {
      let errorString = localize(L.CoinWithdraw.Form.Error.invalidAmount)
      state.coinAmountError = errorString
      state.validationState = .invalid(errorString)
    } else if state.coinAmount.decimalValue! <= 0 {
      let errorString = localize(L.CoinWithdraw.Form.Error.tooLowAmount)
      state.coinAmountError = errorString
      state.validationState = .invalid(errorString)
    } else if !state.coinAmount.decimalValue!.lessThanOrEqualTo(state.maxValue) {
      let errorString = localize(L.CoinWithdraw.Form.Error.tooHighAmount)
      state.coinAmountError = errorString
      state.validationState = .invalid(errorString)
    } else {
      state.coinAmountError = nil
      
      if state.coin?.type == .catm, let fee = state.coinSettings?.txFee {
        let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
        
        if !ethBalance.greaterThanOrEqualTo(fee) {
          let errorString = localize(L.CoinWithdraw.Form.Error.insufficientETHBalance)
          state.coinAmountError = errorString
          state.validationState = .invalid(errorString)
        }
      }
    }
  }
}

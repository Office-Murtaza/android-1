import Foundation
import PhoneNumberKit
import FlagPhoneNumber

enum CoinSendGiftAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalances([CoinBalance])
  case setupCoinSettings(CoinSettings)
  case updateCountry(FPNCountry)
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
  var coinBalances: [CoinBalance]?
  var coinSettings: CoinSettings?
  var country: FPNCountry?
  var phone: String = ""
  var currencyAmount: String = ""
  var coinAmount: String = ""
  var code: String = ""
  var message: String = ""
  var imageId: String?
  var validationState: ValidationState = .unknown
  var shouldShowCodePopup: Bool = false
  
  var coinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == coin?.type }
  }
  
  var maxValue: Double {
    guard let type = coin?.type, let balance = coinBalance?.balance, let fee = coinSettings?.txFee else { return 0 }
    
    if type != .catm {
      return max(0, balance - fee)
    }
    
    let ethBalance = coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
    
    if ethBalance.greaterThanOrEqualTo(fee) {
      return balance
    }
    
    return 0
  }
  
  var phoneE164: String {
    guard let region = country?.code.rawValue else { return "" }
    guard let phoneNumber = try? PhoneNumberKit.default.parse(phone, withRegion: region) else { return "" }
    
    return PhoneNumberKit.default.format(phoneNumber, toType: .e164)
  }
  
  var partialFormatter: PartialFormatter {
    let region = country?.code.rawValue ?? "US"
    return PartialFormatter(defaultRegion: region, withPrefix: false)
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
    case let .updateCountry(country):
      state.country = country
      state.phone = state.partialFormatter.formatPartial(state.phone)
    case let .updatePhone(phone): state.phone = state.partialFormatter.formatPartial(phone ?? "")
    case let .pastePhone(phone): state.phone = state.partialFormatter.formatPartial(phone)
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
    guard state.country != nil && state.phone.count > 0 && state.coinAmount.count > 0 else {
      return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
    }
    
    guard state.phoneE164.count > 0 else {
      return .invalid(localize(L.CoinSendGift.Form.Error.invalidPhone))
    }
    
    guard let amount = state.coinAmount.doubleValue else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
    }
    
    guard amount > 0 else {
      return .invalid(localize(L.CoinWithdraw.Form.Error.tooLowAmount))
    }
    
    guard amount.lessThanOrEqualTo(state.maxValue) else {
      guard state.coin?.type == .catm, let fee = state.coinSettings?.txFee else {
        return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
      }
      
      let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
      
      if ethBalance.greaterThanOrEqualTo(fee) {
        return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
      }
      
      return .invalid(localize(L.CoinWithdraw.Form.Error.insufficientETHBalance))
    }
    
    guard !state.shouldShowCodePopup || state.code.count == 4 else {
      return .invalid(localize(L.CreateWallet.Code.Error.title))
    }
    
    return .valid
  }
}

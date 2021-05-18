import Foundation
import PhoneNumberKit

enum CoinSendGiftAction: Equatable {
    case setupCoin(BTMCoin)
    case setupCoinBalances([CoinBalance])
    case setupCoinDetails(CoinDetails)
    case updatePhone(String?)
    case updateCoinAmount(String?)
    case updateMessage(String?)
    case updatePhoneError(String?)
    case updateCoinAmountError(String?)
    case updateMessageError(String?)
    case updateImage(String?)
    case updateValidationState
    case setupContact(BContact)
    case finishFetchingCoinsData(CoinsBalance, CoinDetails, [BTMCoin])
    
    case updateFromCoin(BTMCoin)
    case updateFromCoinType(CustomCoinType)
    
}

struct CoinSendGiftState: Equatable {
    
    var coin: BTMCoin?
    var coinBalances: [CoinBalance]?
    var coinDetails: CoinDetails?
    var phone: String = ""
    var coinAmount: String = ""
    var message: String = ""
    var phoneError: String?
    var coinAmountError: String?
    var messageError: String?
    var image: String?
    var validationState: ValidationState = .unknown
    var contact: BContact?
    var coins: [BTMCoin]?
    var fromCoin: BTMCoin?
    var fromCoinType: CustomCoinType?
 
    var coinBalance: CoinBalance? {
        return coinBalances?.first { $0.type == coin?.type }
    }
    
    var fromCoinBalance: CoinBalance? {
            return coinBalances?.first { $0.type == fromCoinType}
    }
    
    var fromCoinBalances: [BTMCoin]? {
        return coins?.sorted(by: { $0.index < $1.index })
    }
    
    // to remove
    var maxValue: Decimal {
        guard let type = fromCoin?.type, let balance = fromCoinBalance?.balance, let fee = coinDetails?.txFee else { return 0 }
        
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
    
    var maxFromValue: Decimal {
        guard let type = fromCoinType, let balance = fromCoinBalance?.balance, let fee = coinDetails?.txFee else { return 0 }
        
        if type.isETHBased == true {
            return balance
        } else if type == .ripple {
            return max(0, balance - fee - 20)
        } else {
            return max(0, balance - fee)
        }
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
        case let .setupCoinDetails(coinDetails):
            state.coinDetails = coinDetails
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
        case let .updateImage(image): state.image = image
        case .updateValidationState: validate(&state)
        case let .setupContact(contact):
            state.contact = contact
        case let .finishFetchingCoinsData(balances, details, coins):
            state.coinBalances = balances.coins
            state.coins = coins
            state.coinDetails = details
            let firstCoin = coins.first
            if let type = firstCoin?.type, let address = firstCoin?.address {
                let coin = BTMCoin(type: type, privateKey: "", address: address)
                state.fromCoin = coin
                state.fromCoinType = coin.type
            }
         
        case let .updateFromCoin(coin):
            state.fromCoin = coin
        
        case let .updateFromCoinType(coinType):
            state.coinAmount = ""
            state.fromCoinType = coinType
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
            setupState(with: &state, errorString: L.CreateWallet.Form.Error.fieldRequired)
        } else if state.coinAmount.decimalValue == nil {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.invalidAmount)
        } else if state.coinAmount.decimalValue?.lessThan(state.coinDetails?.txFee ?? 0) == true {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.lessThanFee)
        } else if state.coinAmount.decimalValue! <= 0 {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooLowAmount)
        } else if !state.coinAmount.decimalValue!.lessThanOrEqualTo(state.maxValue) {
            setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.tooHighAmount)
        } else {
            state.coinAmountError = nil
            
            if state.coin?.type.isETHBased ?? false, let fee = state.coinDetails?.txFee {
                let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
                
                if !ethBalance.greaterThanOrEqualTo(fee) {
                    setupState(with: &state, errorString: L.CoinWithdraw.Form.Error.insufficientETHBalance)
                }
            }
        }
    }
    
    private func setupState(with state: inout CoinSendGiftState, errorString: String) {
        let errorString = localize(errorString)
        state.coinAmountError = errorString
        state.validationState = .invalid(errorString)
    }
}

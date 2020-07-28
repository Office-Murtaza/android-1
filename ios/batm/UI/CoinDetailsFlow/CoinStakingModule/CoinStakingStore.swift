import Foundation

enum CoinStakingAction: Equatable {
  case setupCoin(BTMCoin)
  case setupCoinBalances([CoinBalance])
  case setupCoinSettings(CoinSettings)
  case setupStakeDetails(StakeDetails)
  case updateCoinAmount(String?)
  case updateValidationState
  case makeInvalidState(String)
}

struct CoinStakingState: Equatable {
  
  var coin: BTMCoin?
  var coinBalances: [CoinBalance]?
  var coinSettings: CoinSettings?
  var stakeDetails: StakeDetails?
  var coinAmount: String = ""
  var validationState: ValidationState = .unknown
  
  var coinBalance: CoinBalance? {
    return coinBalances?.first { $0.type == coin?.type }
  }
  
  var maxValue: Double {
    guard let type = coin?.type, let balance = coinBalance?.balance, let fee = coinSettings?.txFee else { return 0 }
    
    if type == .catm {
      return balance
    }
    
    return max(0, balance - fee)
  }
  
}

final class CoinStakingStore: ViewStore<CoinStakingAction, CoinStakingState> {
  
  override var initialState: CoinStakingState {
    return CoinStakingState()
  }
  
  override func reduce(state: CoinStakingState, action: CoinStakingAction) -> CoinStakingState {
    var state = state
    
    switch action {
    case let .setupCoin(coin): state.coin = coin
    case let .setupCoinBalances(coinBalances): state.coinBalances = coinBalances
    case let .setupCoinSettings(coinSettings): state.coinSettings = coinSettings
    case let .setupStakeDetails(stakeDetails): state.stakeDetails = stakeDetails
    case let .updateCoinAmount(amount): state.coinAmount = (amount ?? "").coinWithdrawFormatted
    case .updateValidationState: state.validationState = validate(state)
    case let .makeInvalidState(error): state.validationState = .invalid(error)
    }
    
    return state
  }
  
  private func validate(_ state: CoinStakingState) -> ValidationState {
    if state.stakeDetails?.exist == true {
      guard state.coinAmount.isNotEmpty else {
        return .invalid(localize(L.CreateWallet.Form.Error.allFieldsRequired))
      }
    }
    
    if state.stakeDetails?.exist == false {
      guard let amount = state.coinAmount.doubleValue else {
        return .invalid(localize(L.CoinWithdraw.Form.Error.invalidAmount))
      }
      
      guard amount > 0 else {
        return .invalid(localize(L.CoinWithdraw.Form.Error.tooLowAmount))
      }
      
      guard amount.lessThanOrEqualTo(state.maxValue) else {
        return .invalid(localize(L.CoinWithdraw.Form.Error.tooHighAmount))
      }
      
      if state.coin?.type == .catm, let fee = state.coinSettings?.txFee {
        let ethBalance = state.coinBalances?.first { $0.type == .ethereum }?.balance ?? 0
        
        if !ethBalance.greaterThanOrEqualTo(fee) {
          return .invalid(localize(L.CoinWithdraw.Form.Error.insufficientETHBalance))
        }
      }
    }
    
    return .valid
  }
}

import Foundation
import RxSwift
import PhoneNumberKit

enum CoinsBalanceAction: Equatable {
  case updateCoinsBalance(CoinsBalance?)
}

struct CoinsBalanceState: Equatable {
  
  var coinsBalance: CoinsBalance?
  
}

final class CoinsBalanceStore: ViewStore<CoinsBalanceAction, CoinsBalanceState> {
  
  override var initialState: CoinsBalanceState {
    return CoinsBalanceState()
  }
  
  override func reduce(state: CoinsBalanceState, action: CoinsBalanceAction) -> CoinsBalanceState {
    var state = state
    
    switch action {
    case let .updateCoinsBalance(coinsBalance): state.coinsBalance = coinsBalance
    }
    
    return state
  }
}

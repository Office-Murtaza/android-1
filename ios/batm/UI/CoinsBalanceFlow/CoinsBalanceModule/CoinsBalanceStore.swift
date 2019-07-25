import Foundation

enum CoinsBalanceAction: Equatable {
  case startFetching
  case finishFetching(CoinsBalance)
}

struct CoinsBalanceState: Equatable {
  
  var coinsBalance: CoinsBalance?
  var isFetching: Bool = false
  
}

final class CoinsBalanceStore: ViewStore<CoinsBalanceAction, CoinsBalanceState> {
  
  override var initialState: CoinsBalanceState {
    return CoinsBalanceState()
  }
  
  override func reduce(state: CoinsBalanceState, action: CoinsBalanceAction) -> CoinsBalanceState {
    var state = state
    
    switch action {
    case .startFetching: state.isFetching = true
    case let .finishFetching(coinsBalance):
      state.isFetching = false
      state.coinsBalance = coinsBalance
    }
    
    return state
  }
}

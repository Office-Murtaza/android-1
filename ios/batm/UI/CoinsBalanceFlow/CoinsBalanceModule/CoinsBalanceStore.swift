import Foundation

enum CoinsBalanceAction: Equatable {
  case startFetching
  case finishFetching
  case finishFetchingCoinsBalance(CoinsBalance)
}

struct CoinsBalanceState: Equatable {
  
  var coinsBalance: CoinsBalance?
  var isFetching: Bool = false
  
  var coins: [CoinBalance]? {
    return coinsBalance?.coins.sorted()
  }
  
}

final class CoinsBalanceStore: ViewStore<CoinsBalanceAction, CoinsBalanceState> {
  
  override var initialState: CoinsBalanceState {
    return CoinsBalanceState()
  }
  
  override func reduce(state: CoinsBalanceState, action: CoinsBalanceAction) -> CoinsBalanceState {
    var state = state
    
    switch action {
    case .startFetching: state.isFetching = true
    case .finishFetching: state.isFetching = false
    case let .finishFetchingCoinsBalance(coinsBalance):
      state.isFetching = false
      state.coinsBalance = coinsBalance
    }
    
    return state
  }
}

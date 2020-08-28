import Foundation

enum WalletAction: Equatable {
  case startFetching
  case finishFetching
  case finishFetchingCoinsBalance(CoinsBalance)
}

struct WalletState: Equatable {
  
  var coinsBalance: CoinsBalance = .empty
  var isFetching: Bool = false
  
  var coins: [CoinBalance] {
    return coinsBalance.coins.sorted()
  }
  
}

final class WalletStore: ViewStore<WalletAction, WalletState> {
  
  override var initialState: WalletState {
    return WalletState()
  }
  
  override func reduce(state: WalletState, action: WalletAction) -> WalletState {
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

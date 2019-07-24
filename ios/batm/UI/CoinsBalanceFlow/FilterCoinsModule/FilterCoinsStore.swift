import Foundation

enum FilterCoinsAction: Equatable {
  case updateCoins([BTMCoin])
}

struct FilterCoinsState: Equatable {
  
  var coins: [BTMCoin] = []
  
}

final class FilterCoinsStore: ViewStore<FilterCoinsAction, FilterCoinsState> {
  
  override var initialState: FilterCoinsState {
    return FilterCoinsState()
  }
  
  override func reduce(state: FilterCoinsState, action: FilterCoinsAction) -> FilterCoinsState {
    var state = state
    
    switch action {
    case let .updateCoins(coins): state.coins = coins.sorted { $0.type.verboseValue < $1.type.verboseValue }
    }
    
    return state
  }
}

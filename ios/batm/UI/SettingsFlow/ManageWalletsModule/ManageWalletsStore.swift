import Foundation

enum ManageWalletsAction: Equatable {
  case updateCoins([BTMCoin])
}

struct ManageWalletsState: Equatable {
  
  var coins: [BTMCoin] = []
  
}

final class ManageWalletsStore: ViewStore<ManageWalletsAction, ManageWalletsState> {
  
  override var initialState: ManageWalletsState {
    return ManageWalletsState()
  }
  
  override func reduce(state: ManageWalletsState, action: ManageWalletsAction) -> ManageWalletsState {
    var state = state
    
    switch action {
    case let .updateCoins(coins): state.coins = coins.sorted { $0.index < $1.index }
    }
    
    return state
  }
}

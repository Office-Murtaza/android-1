import Foundation

enum ATMAction: Equatable {
  case updateMapAddresses(MapAddresses)
}

struct ATMState: Equatable {
  
  var mapAddresses: MapAddresses?
  
}

final class ATMStore: ViewStore<ATMAction, ATMState> {
  
  override var initialState: ATMState {
    return ATMState()
  }
  
  override func reduce(state: ATMState, action: ATMAction) -> ATMState {
    var state = state
    
    switch action {
    case let .updateMapAddresses(mapAddresses): state.mapAddresses = mapAddresses
    }
    
    return state
  }
}

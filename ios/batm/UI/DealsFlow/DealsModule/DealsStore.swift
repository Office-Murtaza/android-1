import Foundation

enum DealsAction: Equatable {}

struct DealsState: Equatable {}

final class DealsStore: ViewStore<DealsAction, DealsState> {
  override var initialState: DealsState {
    return .init()
  }
  
  override func reduce(state: DealsState, action: DealsAction) -> DealsState {
    var state = state
    
    return state
  }
}

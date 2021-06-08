import UIKit

enum OrderDetailsActionType {
  case none
  case cancel
  case doing
  case paid
  case release
  case disput
  
  var networkType: Int {
    switch self {
    case .none: return -1
    case .cancel: return 2
    case .doing: return 3
    case .paid: return 4
    case .release: return 5
    case .disput: return 6
    }
  }
}

struct OrderDetailsActionViewModel {
  let types: [OrderDetailsActionType]
  
  func actionViews() -> [OrderDetailsActionView] {
    return types.filter { $0 != .none }
      .map { type in
        let view = OrderDetailsActionView()
        view.update(type: type)
        return view
      }
  }
}

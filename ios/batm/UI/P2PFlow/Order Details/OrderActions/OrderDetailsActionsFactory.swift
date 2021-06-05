import Foundation

struct OrderDetailsActionsFactory {
  
  func generateOrderActions(type: P2PSellBuyViewType, status: TradeOrderStatus) -> OrderDetailsActionViewModel {
    switch type {
    case .buy: return buyerActions(status: status)
    case .sell: return sellerActions(status: status)
    }
  }
  
  private func sellerActions(status: TradeOrderStatus) -> OrderDetailsActionViewModel {
    switch status {
    case .new: return OrderDetailsActionViewModel(types: [.cancel])
    case .canceled: return OrderDetailsActionViewModel(types: [.none])
    case .doing: return OrderDetailsActionViewModel(types: [.none])
    case .paid: return OrderDetailsActionViewModel(types: [.release, .disput])
    case .released: return OrderDetailsActionViewModel(types: [.none])
    case .disputing: return OrderDetailsActionViewModel(types: [.none])
    case .solved: return OrderDetailsActionViewModel(types: [.none])
    }
  }
  
  private func buyerActions(status: TradeOrderStatus) -> OrderDetailsActionViewModel {
    switch status {
    case .new: return OrderDetailsActionViewModel(types: [.doing, .cancel])
    case .canceled: return OrderDetailsActionViewModel(types: [.none])
    case .doing: return OrderDetailsActionViewModel(types: [.paid])
    case .paid: return OrderDetailsActionViewModel(types: [.disput])
    case .released: return OrderDetailsActionViewModel(types: [.none])
    case .disputing: return OrderDetailsActionViewModel(types: [.none])
    case .solved: return OrderDetailsActionViewModel(types: [.none])
    }
  }
}

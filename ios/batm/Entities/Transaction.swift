import UIKit

enum TransactionType {
  case unknown
  case deposit
  case withdraw
  case sendGift
  case receiveGift
  case buy
  case sell
  
  var verboseValue: String {
    switch self {
    case .unknown: return localize(L.CoinDetails.unknown)
    case .deposit: return localize(L.CoinDetails.deposit)
    case .withdraw: return localize(L.CoinDetails.withdraw)
    case .sendGift: return localize(L.CoinDetails.sendGift)
    case .receiveGift: return localize(L.CoinDetails.receiveGift)
    case .buy: return localize(L.CoinDetails.buy)
    case .sell: return localize(L.CoinDetails.sell)
    }
  }
  
  init(rawValue: Int) {
    switch rawValue {
    case 0: self = .unknown
    case 1: self = .deposit
    case 2: self = .withdraw
    case 3: self = .sendGift
    case 4: self = .receiveGift
    case 5: self = .buy
    case 6: self = .sell
    default: self = .unknown
    }
  }
}

enum TransactionStatus {
  case unknown
  case pending
  case complete
  case fail
  
  var verboseValue: String {
    switch self {
    case .pending: return localize(L.CoinDetails.pending)
    case .complete: return localize(L.CoinDetails.complete)
    case .fail: return localize(L.CoinDetails.fail)
    case .unknown: return localize(L.CoinDetails.unknown)
    }
  }
  
  var associatedColor: UIColor {
    switch self {
    case .pending: return .lightGold
    case .complete: return .darkMint
    case .fail: return .tomato
    case .unknown: return .warmGrey
    }
  }
  
  init(rawValue: Int) {
    switch rawValue {
    case 0: self = .unknown
    case 1: self = .pending
    case 2: self = .complete
    case 3: self = .fail
    default: self = .unknown
    }
  }
}

struct Transaction: Equatable {
  let dateString: String
  let type: TransactionType
  let status: TransactionStatus
  let amount: Double
}

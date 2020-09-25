import UIKit

enum TransactionType {
  case unknown
  case deposit
  case withdraw
  case sendGift
  case receiveGift
  case buy
  case sell
  case move
  case sendС2С
  case receiveС2С
  case reserve
  case recall
  case `self`
  case stake
  case unstake
  
  var verboseValue: String {
    switch self {
    case .unknown: return localize(L.CoinDetails.unknown)
    case .deposit: return localize(L.CoinDetails.deposit)
    case .withdraw: return localize(L.CoinDetails.withdraw)
    case .sendGift: return localize(L.CoinDetails.sendGift)
    case .receiveGift: return localize(L.CoinDetails.receiveGift)
    case .buy: return localize(L.CoinDetails.buy)
    case .sell: return localize(L.CoinDetails.sell)
    case .move: return localize(L.CoinDetails.move)
    case .sendС2С: return localize(L.CoinDetails.sendC2C)
    case .receiveС2С: return localize(L.CoinDetails.receiveC2C)
    case .reserve: return localize(L.CoinDetails.reserve)
    case .recall: return localize(L.CoinDetails.recall)
    case .self: return localize(L.CoinDetails.se1f)
    case .stake: return localize(L.CoinDetails.stake)
    case .unstake: return localize(L.CoinDetails.unstake)
    }
  }
  
  var rawValue: Int {
    switch self {
    case .unknown: return 0
    case .deposit: return 1
    case .withdraw: return 2
    case .sendGift: return 3
    case .receiveGift: return 4
    case .buy: return 5
    case .sell: return 6
    case .move: return 7
    case .sendС2С: return 8
    case .receiveС2С: return 9
    case .reserve: return 10
    case .recall: return 11
    case .self: return 12
    case .stake: return 13
    case .unstake: return 14
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
    case 7: self = .move
    case 8: self = .sendС2С
    case 9: self = .receiveС2С
    case 10: self = .reserve
    case 11: self = .recall
    case 12: self = .self
    case 13: self = .stake
    case 14: self = .unstake
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

enum TransactionCashStatus {
  case unknown
  case notAvailable
  case available
  case withdrawn
  
  var verboseValue: String {
    switch self {
    case .notAvailable: return localize(L.CoinDetails.notAvailable)
    case .available: return localize(L.CoinDetails.available)
    case .withdrawn: return localize(L.CoinDetails.withdrawn)
    case .unknown: return localize(L.CoinDetails.unknown)
    }
  }
  
  var associatedColor: UIColor {
    switch self {
    case .notAvailable: return .tomato
    case .available: return .darkMint
    case .withdrawn: return .pinkishGrey
    case .unknown: return .warmGrey
    }
  }
  
  init(rawValue: Int) {
    switch rawValue {
    case 0: self = .notAvailable
    case 1: self = .available
    case 2: self = .withdrawn
    default: self = .unknown
    }
  }
}

struct Transaction: Equatable {
  let txId: String?
  let txDbId: String?
  let dateString: String
  let type: TransactionType
  let status: TransactionStatus
  let amount: Decimal
}

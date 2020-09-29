import UIKit

struct StakeDetails: Equatable {
  var created: Bool
  var canceled: Bool
  var withdrawn: Bool
  var amount: Decimal?
  var rewardAmount: Decimal?
  var rewardPercent: Decimal?
  var rewardAnnualAmount: Decimal?
  var rewardAnnualPercent: Decimal
  var createDateString: String?
  var cancelDateString: String?
  var duration: Int?
  var untilWithdraw: Int?
  var cancelPeriod: Int
  
  var status: StakingStatus {
    if !created || withdrawn {
      return .notCreatedOrWithdrawn
    }
    
    if !canceled {
      return .created
    }
    
    return .canceled
  }
}

enum StakingStatus {
  case notCreatedOrWithdrawn
  case created
  case canceled
  
  var verboseValue: String {
    switch self {
    case .notCreatedOrWithdrawn: return ""
    case .created: return localize(L.CoinStaking.Status.created)
    case .canceled: return localize(L.CoinStaking.Status.canceled)
    }
  }
  
  var associatedColor: UIColor {
    switch self {
    case .notCreatedOrWithdrawn: return .clear
    case .created: return .lightGold
    case .canceled: return .pastelOrange
    }
  }
}

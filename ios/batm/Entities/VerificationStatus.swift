import UIKit

enum VerificationStatus {
  case unknown
  case notVerified
  case verificationPending
  case verificationRejected
  case verified
  case vipVerificationPending
  case vipVerificationRejected
  case vipVerified
  
  var needVerification: Bool {
    return self == .notVerified || self == .verificationRejected
  }
  
  var needVIPVerification: Bool {
    return self == .verified || self == .vipVerificationRejected
  }
  
  var needAnyVerification: Bool {
    return needVerification || needVIPVerification
  }
  
  var verboseValue: String {
    switch self {
    case .unknown: return localize(L.VerificationInfo.StatusRow.Value.unknown)
    case .notVerified: return localize(L.VerificationInfo.StatusRow.Value.notVerified)
    case .verificationPending: return localize(L.VerificationInfo.StatusRow.Value.verificationPending)
    case .verificationRejected: return localize(L.VerificationInfo.StatusRow.Value.verificationRejected)
    case .verified: return localize(L.VerificationInfo.StatusRow.Value.verified)
    case .vipVerificationPending: return localize(L.VerificationInfo.StatusRow.Value.vipVerificationPending)
    case .vipVerificationRejected: return localize(L.VerificationInfo.StatusRow.Value.vipVerificationRejected)
    case .vipVerified: return localize(L.VerificationInfo.StatusRow.Value.vipVerified)
    }
  }
  
  var associatedColor: UIColor {
    switch self {
    case .unknown: return .warmGrey
    case .notVerified: return .pinkishGrey
    case .verificationPending: return .lightGold
    case .verificationRejected: return .tomato
    case .verified: return .darkMint
    case .vipVerificationPending: return .mango
    case .vipVerificationRejected: return .puce
    case .vipVerified: return .ceruleanBlue
    }
  }
  
  init(rawValue: Int) {
    switch rawValue {
    case 0: self = .unknown
    case 1: self = .notVerified
    case 2: self = .verificationPending
    case 3: self = .vipVerificationPending
    case 4: self = .verified
    case 5: self = .vipVerificationPending
    case 6: self = .vipVerificationRejected
    case 7: self = .vipVerified
    default: self = .unknown
    }
  }
}

struct VerificationInfo: Equatable {
  var txLimit: Double
  var dailyLimit: Double
  var status: VerificationStatus
  var message: String?
  
  static var empty: VerificationInfo {
    return VerificationInfo(txLimit: 0, dailyLimit: 0, status: .notVerified, message: nil)
  }
}

import UIKit

enum KYCStatus {
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
    case .unknown: return localize(L.KYC.Header.Status.Value.unknown)
    case .notVerified: return localize(L.KYC.Header.Status.Value.notVerified)
    case .verificationPending: return localize(L.KYC.Header.Status.Value.verificationPending)
    case .verificationRejected: return localize(L.KYC.Header.Status.Value.verificationRejected)
    case .verified: return localize(L.KYC.Header.Status.Value.verified)
    case .vipVerificationPending: return localize(L.KYC.Header.Status.Value.vipVerificationPending)
    case .vipVerificationRejected: return localize(L.KYC.Header.Status.Value.vipVerificationRejected)
    case .vipVerified: return localize(L.KYC.Header.Status.Value.vipVerified)
    }
  }
  
  var associatedColor: UIColor {
    switch self {
    case .unknown, .notVerified: return .greyishTwo
    case .verificationPending, .vipVerificationPending: return .lightGold
    case .verificationRejected, .vipVerificationRejected: return .tomato
    case .verified: return .paleOliveGreen
    case .vipVerified: return .darkMint
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

struct KYC: Equatable {
  var txLimit: Double
  var dailyLimit: Double
  var status: KYCStatus
  var message: String?
  
  static var empty: KYC {
    return KYC(txLimit: 0, dailyLimit: 0, status: .notVerified, message: nil)
  }
}

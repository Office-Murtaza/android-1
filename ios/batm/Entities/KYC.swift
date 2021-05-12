import UIKit

enum KYCStatus: Int {
    case unknown = 0
    case notVerified = 1
    case verificationPending = 2
    case verificationRejected = 3
    case verified = 4
    case vipVerificationPending = 5
    case vipVerificationRejected = 6
    case vipVerified = 7
    
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
    
    var associatedImage: UIImage? {
        switch self {
        case .unknown: return UIImage.unknown
        case .notVerified: return UIImage.KYCStatus.notVerified
        case .verificationPending: return UIImage.KYCStatus.verificationPending
        case .verificationRejected: return UIImage.KYCStatus.verificationRejected
        case .verified: return UIImage.KYCStatus.verified
        case .vipVerificationPending: return UIImage.KYCStatus.vipVerificationPending
        case .vipVerificationRejected: return UIImage.KYCStatus.vipVerificationRejected
        case .vipVerified: return UIImage.KYCStatus.vipVerified
        }
    }
}

struct KYC: Equatable {
    var txLimit: Decimal
    var dailyLimit: Decimal
    var status: KYCStatus
    var message: String?
    
    static var empty: KYC {
        return KYC(txLimit: 0, dailyLimit: 0, status: .notVerified, message: nil)
    }
}

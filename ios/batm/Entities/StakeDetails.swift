import UIKit

struct StakeDetails: Equatable {
    var status: StakingStatus
    var amount: Double?
    var amountStr: String?
    var rewardAmount: Double?
    var rewardAmountStr: String?
    var rewardPercent: Double?
    var rewardPercentStr: String?
    var rewardAnnualAmount: Double?
    var rewardAnnualAmountStr: String?
    var annualPercent: Int?
    var createDate: String?
    var cancelDate: String?
    var duration: Int?
    var tillWithdrawal: Int?
    var holdPeriod: Int?
}

enum StakingStatus: Int, Equatable {
    case notExist = 1
    case createPending = 2
    case created = 3
    case cancelPending = 4
    case canceled = 5
    case withdrawPending = 6
    case withdrawn = 7
    
    var verboseValue: String {
        switch self {
        case .notExist: return ""
        case .createPending: return localize(L.CoinStaking.Status.createPending)
        case .created: return localize(L.CoinStaking.Status.created)
        case .cancelPending: return localize(L.CoinStaking.Status.cancelPending)
        case .canceled: return localize(L.CoinStaking.Status.canceled)
        case .withdrawPending: return localize(L.CoinStaking.Status.withdrawPending)
        case .withdrawn: return ""
        }
    }
    
    var associatedColor: StatusColorPallete.Type {
        switch self {
        case .notExist: return UIColor.StatusColor.Gray.self
        case .createPending: return UIColor.StatusColor.Yellow.self
        case .created: return UIColor.StatusColor.Green.self
        case .cancelPending: return UIColor.StatusColor.Orange.self
        case .canceled: return UIColor.StatusColor.Red.self
        case .withdrawPending: return UIColor.StatusColor.Pink.self
        case .withdrawn: return UIColor.StatusColor.Blue.self
        }
    }
}

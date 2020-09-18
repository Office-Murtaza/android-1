import Foundation

struct StakeDetails: Equatable {
  var exist: Bool
  var amount: Decimal?
  var rewardAmount: Decimal?
  var rewardPercent: Decimal?
  var rewardAnnualAmount: Decimal?
  var rewardAnnualPercent: Decimal
  var days: Int?
  var minDays: Int
}

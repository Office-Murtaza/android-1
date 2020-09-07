import Foundation

struct StakeDetails: Equatable {
  var exist: Bool
  var unstakeAvailable: Bool
  var stakedAmount: Decimal?
  var rewardsAmount: Decimal?
  var rewardsPercent: Decimal?
  var stakedDays: Int?
  var stakingMinDays: Int?
}

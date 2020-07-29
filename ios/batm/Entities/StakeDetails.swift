import Foundation

struct StakeDetails: Equatable {
  var exist: Bool
  var unstakeAvailable: Bool
  var stakedAmount: Double?
  var rewardsAmount: Double?
  var rewardsPercent: Double?
  var stakedDays: Int?
  var stakingMinDays: Int?
}

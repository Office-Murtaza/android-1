import ObjectMapper

extension StakeDetails: ImmutableMappable {
  init(map: Map) throws {
    exist = try map.value("exist")
    unstakeAvailable = try map.value("unstakeAvailable")
    stakedAmount = try? map.value("stakedAmount", using: DecimalDoubleTransform())
    rewardsAmount = try? map.value("rewardsAmount", using: DecimalDoubleTransform())
    rewardsPercent = try? map.value("rewardsPercent", using: DecimalDoubleTransform())
    stakedDays = try map.value("stakedDays")
    stakingMinDays = try map.value("stakingMinDays")
  }
}

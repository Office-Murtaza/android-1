import ObjectMapper

extension StakeDetails: ImmutableMappable {
  init(map: Map) throws {
    exist = try map.value("exist")
    stakedAmount = try map.value("stakedAmount")
    rewardsAmount = try map.value("rewardsAmount")
    rewardsPercent = try map.value("rewardsPercent")
    stakedDays = try map.value("stakedDays")
  }
}

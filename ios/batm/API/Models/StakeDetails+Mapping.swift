import ObjectMapper

extension StakeDetails: ImmutableMappable {
  init(map: Map) throws {
    guard let rewardAnnualPercent = Decimal(string: try map.value("rewardAnnualPercentStr")) else {
      throw ObjectMapperError.couldNotMap
    }
    
    let amount: String? = try map.value("amountStr")
    let rewardAmount: String? = try map.value("rewardAmountStr")
    let rewardPercent: String? = try map.value("rewardPercentStr")
    let rewardAnnualAmount: String? = try map.value("rewardAnnualAmountStr")
    
    exist = try map.value("exist")
    self.amount = amount.flatMap { Decimal(string: $0) }
    self.rewardAmount = rewardAmount.flatMap { Decimal(string: $0) }
    self.rewardPercent = rewardPercent.flatMap { Decimal(string: $0) }
    self.rewardAnnualAmount = rewardAnnualAmount.flatMap { Decimal(string: $0) }
    self.rewardAnnualPercent = rewardAnnualPercent
    days = try map.value("days")
    minDays = try map.value("minDays")
  }
}

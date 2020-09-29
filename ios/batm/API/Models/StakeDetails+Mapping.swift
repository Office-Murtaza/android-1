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
    
    created = try map.value("created")
    canceled = try map.value("canceled")
    withdrawn = try map.value("withdrawn")
    self.amount = amount.flatMap { Decimal(string: $0) }
    self.rewardAmount = rewardAmount.flatMap { Decimal(string: $0) }
    self.rewardPercent = rewardPercent.flatMap { Decimal(string: $0) }
    self.rewardAnnualAmount = rewardAnnualAmount.flatMap { Decimal(string: $0) }
    self.rewardAnnualPercent = rewardAnnualPercent
    createDateString = try map.value("createDate")
    cancelDateString = try map.value("cancelDate")
    duration = try map.value("duration")
    untilWithdraw = try map.value("untilWithdraw")
    cancelPeriod = try map.value("cancelPeriod")
  }
}

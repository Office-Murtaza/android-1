import ObjectMapper

extension StakeDetails: ImmutableMappable {
    init(map: Map) throws {
        status = try map.value("status")
        amount = try map.value("amount")
        amountStr = try map.value("amountStr")
        rewardAmount = try map.value("rewardAmount")
        rewardAmountStr = try map.value("rewardAmountStr")
        rewardPercent = try map.value("rewardPercent")
        rewardPercentStr = try map.value("rewardPercentStr")
        rewardAnnualAmount = try map.value("rewardAnnualAmount")
        rewardAnnualAmountStr = try map.value("rewardAnnualAmountStr")
        rewardAnnualPercent = try map.value("rewardAnnualPercent")
        createDate = try map.value("createDate")
        cancelDate = try map.value("cancelDate")
        duration = try map.value("duration")
        untilWithdraw = try map.value("untilWithdraw")
        holdPeriod = try map.value("holdPeriod")
    }
}

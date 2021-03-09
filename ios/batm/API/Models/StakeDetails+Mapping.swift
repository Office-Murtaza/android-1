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
        annualPercent = try map.value("annualPercent")
        let createTime: Int? = try map.value("createTimestamp")
        createTimestamp = createTime?.toDate(format: GlobalConstants.shortDateForm)
        let cancelTime: Int? = try map.value("cancelTimestamp")
        cancelTimestamp = cancelTime?.toDate(format: GlobalConstants.shortDateForm)
        duration = try map.value("duration")
        tillWithdrawal = try map.value("tillWithdrawal")
        holdPeriod = try map.value("holdPeriod")
    }
}

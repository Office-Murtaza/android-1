import ObjectMapper

extension StakeDetails: ImmutableMappable {
    init(map: Map) throws {
        id = try? map.value("id")
        coin = try? map.value("coin")
        let statusResponse: Int = (try? map.value("status")) ?? 0
        status = StakingStatus(rawValue: statusResponse)
        cryptoAmount = try? map.value("cryptoAmount")
        let base: Int = (try? map.value("basePeriod")) ?? 1
        basePeriod = base
        annualPeriod = try? map.value("annualPeriod")
        let holdPeriodTime: Int = (try? map.value("holdPeriod")) ?? 0
        holdPeriod = holdPeriodTime / base
        annualPercent = try? map.value("annualPercent")
        createTxId = try? map.value("createTxId")
        let createTime: Int = (try? map.value("createTimestamp")) ?? 0
        createTimestamp = createTime.timestampToStringDate(format: GlobalConstants.shortDateForm)
        cancelTxId = try? map.value("cancelTxId")
        let cancelTime: Int? = try? map.value("cancelTimestamp")
        cancelTimestamp = cancelTime?.timestampToStringDate(format: GlobalConstants.shortDateForm)
        
        withdrawTxId = try? map.value("withdrawTxId")
        let withdrawTime: Int? = try? map.value("withdrawTimestamp")
        withdrawTimestamp = withdrawTime?.timestampToStringDate(format: GlobalConstants.shortDateForm)

        let durationTime: Int = cancelTime != nil ? cancelTime ?? 0 : Int(TimeInterval())
        
        duration = (durationTime - (createTime)) / 1000 / base
        rewardPercent = duration * ((annualPercent ?? 0) / (annualPeriod  ?? 0) / base)
        rewardAmount = (cryptoAmount ?? 0.0) * Double((rewardPercent)) / 100
        rewardAnnualAmount = (cryptoAmount ?? 0.0) * Double((annualPercent ?? 0)) / 100
        let some = (holdPeriod ?? 0) / base
        let some1 = (cancelTime ?? 0) - createTime
        tillWithdraw = max(0, some - some1 / 1000 / base)
    }
}

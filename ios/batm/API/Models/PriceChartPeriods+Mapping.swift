import ObjectMapper

extension PriceChartPeriods: ImmutableMappable {
  init(map: Map) throws {
    oneDayPeriod = try map.value("day")
    oneWeekPeriod = try map.value("week")
    oneMonthPeriod = try map.value("month")
    threeMonthsPeriod = try map.value("threeMonths")
    oneYearPeriod = try map.value("year")
  }
}


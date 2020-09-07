import Foundation

struct PriceChartData: Equatable {
  var price: Decimal
  var balance: Decimal
  var periods: PriceChartPeriods
}

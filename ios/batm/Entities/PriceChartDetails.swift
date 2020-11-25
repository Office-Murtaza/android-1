import Foundation

enum PriceChartDetailsPeriod: Int {
  case day = 1
  case week
  case month
  case year
}

struct PriceChartDetails: Equatable {
   let prices: [[Double]]
}

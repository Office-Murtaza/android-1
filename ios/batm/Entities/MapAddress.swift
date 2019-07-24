import Foundation

struct MapAddress: Equatable {
  let name: String
  let address: String
  let latitude: Double
  let longitude: Double
  let openHours: [OpenHour]
}

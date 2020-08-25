import Foundation

struct Country: Codable {
  let code: String
  let name: String
  let states: [State]
}

struct State: Codable {
  let code: String
  let name: String
  let cities: [String]
}

enum VerificationConstants {
  static let countries: [Country] = {
    let url = Bundle.main.url(forResource: "countries", withExtension: "json")!
    let data = try! Data(contentsOf: url)
    let decoder = JSONDecoder()
    let countries = try! decoder.decode([Country].self, from: data)
    return countries
  }()
}

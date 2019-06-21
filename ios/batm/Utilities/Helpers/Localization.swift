import Foundation

func localize(_ key: String) -> String {
  return NSLocalizedString(key, comment: "")
}

func pluralize(_ key: String, _ value1: Int) -> String {
  return String.localizedStringWithFormat(localize(key), value1)
}

func pluralize(_ key: String, _ value1: Int, _ value2: Int) -> String {
  return String.localizedStringWithFormat(localize(key), value1, value2)
}

import UIKit

enum AboutCellType: CaseIterable, SettingsCellTypeRepresentable {
  case termsAndConditions
  case version

  var title: String {
    switch self {
    case .termsAndConditions: return localize(L.About.Cell.termsAndConditions)
    case .version: return localize(L.About.Cell.version)
    }
  }
  
  var value: String? {
    switch self {
    case .version: return UIApplication.appVersion
    default: return nil
    }
  }
  
  var isEnabled: Bool {
    switch self {
    case .version: return false
    default: return true
    }
  }
}

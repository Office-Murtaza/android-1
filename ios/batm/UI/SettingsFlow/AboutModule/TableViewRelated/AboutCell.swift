import UIKit

enum AboutCellType: CaseIterable, SettingsCellTypeRepresentable {
  private enum Const {
    static let privacyPolicy = "https://www.belcobtm.com/privacy-policy"
    static let compliantPolicy = "https://www.belcobtm.com/complaint-policy"
    static let termsAndConditions = "https://www.belcobtm.com/terms-and-conditions"
  }
    
  case privacyPolicy
  case compliantPolicy
  case termsAndConditions
  case version

  var title: String {
    switch self {
    case .privacyPolicy: return localize(L.About.Cell.privacyPolicy)
    case .compliantPolicy: return localize(L.About.Cell.compliantPolicy)
    case .termsAndConditions: return localize(L.About.Cell.termsAndConditions)
    case .version: return localize(L.About.Cell.version)
    }
  }
  
  var value: String? {
    switch self {
    case .version: return UIApplication.shared.appVersion
    default: return nil
    }
  }
    
  var link: String {
    switch self {
    case .privacyPolicy: return Const.privacyPolicy
    case .compliantPolicy: return Const.compliantPolicy
    case .termsAndConditions: return Const.termsAndConditions
    default: return ""
    }
  }
  
  var isEnabled: Bool {
    switch self {
    case .version: return false
    default: return true
    }
  }
    
  var isDisclosureNeeded: Bool {
    switch self {
    case .version: return false
    default: return true
    }
  }
}

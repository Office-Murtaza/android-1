import UIKit

enum SecurityCellType: CaseIterable, SettingsCellTypeRepresentable {
  case updatePhone
  case updatePassword
  case updatePIN
  case seedPhrase
  case unlink

  var title: String {
    switch self {
    case .updatePhone: return localize(L.Security.Cell.updatePhone)
    case .updatePassword: return localize(L.Security.Cell.updatePassword)
    case .updatePIN: return localize(L.Security.Cell.updatePIN)
    case .seedPhrase: return localize(L.Security.Cell.seedPhrase)
    case .unlink: return localize(L.Security.Cell.unlink)
    }
  }

  var image: UIImage? {
    switch self {
    case .updatePhone: return UIImage(named: "security_phone")
    case .updatePassword: return UIImage(named: "security_password")
    case .updatePIN: return UIImage(named: "security_pin")
    case .seedPhrase: return UIImage(named: "security_seed_phrase")
    case .unlink: return UIImage(named: "security_unlink")
    }
  }
}

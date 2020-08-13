import UIKit

enum SecurityCellType: CaseIterable, SettingsCellTypeRepresentable {
  case updatePhone
  case updatePassword
  case updatePIN
  case seedPhrase
  case unlinkWallet

  var title: String {
    switch self {
    case .updatePhone: return localize(L.Security.Cell.updatePhone)
    case .updatePassword: return localize(L.Security.Cell.updatePassword)
    case .updatePIN: return localize(L.Security.Cell.updatePIN)
    case .seedPhrase: return localize(L.Security.Cell.seedPhrase)
    case .unlinkWallet: return localize(L.Security.Cell.unlinkWallet)
    }
  }

  var image: UIImage? {
    switch self {
    case .updatePhone: return UIImage(named: "security_phone")
    case .updatePassword: return UIImage(named: "security_password")
    case .updatePIN: return UIImage(named: "security_pin")
    case .seedPhrase: return UIImage(named: "security_seed_phrase")
    case .unlinkWallet: return UIImage(named: "security_unlink")
    }
  }
}

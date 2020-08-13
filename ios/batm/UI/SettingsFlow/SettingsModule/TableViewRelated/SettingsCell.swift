import UIKit
import RxSwift
import RxCocoa

enum SettingsCellType: CaseIterable, SettingsCellTypeRepresentable {
  case security
  case kyc
  case about

  var title: String {
    switch self {
    case .security: return localize(L.Settings.Cell.security)
    case .kyc: return localize(L.Settings.Cell.kyc)
    case .about: return localize(L.Settings.Cell.about)
    }
  }

  var image: UIImage? {
    switch self {
    case .security: return UIImage(named: "settings_security")
    case .kyc: return UIImage(named: "settings_kyc")
    case .about: return UIImage(named: "settings_about")
    }
  }
}

protocol SettingsCellTypeRepresentable {
  var title: String { get }
  var image: UIImage? { get }
}

final class SettingsCell: UITableViewCell {
  
  func configure(for type: SettingsCellTypeRepresentable) {
    imageView?.image = type.image
    textLabel?.text = type.title
    textLabel?.textColor = .slateGrey
    textLabel?.font = .systemFont(ofSize: 16, weight: .medium)
  }
}

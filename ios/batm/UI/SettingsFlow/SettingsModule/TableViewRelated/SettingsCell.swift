import UIKit
import RxSwift
import RxCocoa

protocol SettingsCellTypeRepresentable {
  var title: String { get }
  var image: UIImage? { get }
  var value: String? { get }
  var isEnabled: Bool { get }
}

extension SettingsCellTypeRepresentable {
  var image: UIImage? { return nil }
  var value: String? { return nil }
  var isEnabled: Bool { return true }
}

enum SettingsCellType: CaseIterable, SettingsCellTypeRepresentable {
  case wallet
  case security
  case kyc
  case support
  case about

  var title: String {
    switch self {
    case .wallet: return localize(L.Settings.Cell.wallet)
    case .security: return localize(L.Settings.Cell.security)
    case .kyc: return localize(L.Settings.Cell.kyc)
    case .support: return localize(L.Settings.Cell.support)
    case .about: return localize(L.Settings.Cell.about)
    }
  }

  var image: UIImage? {
    switch self {
    case .wallet: return UIImage(named: "settings_wallet")
    case .security: return UIImage(named: "settings_security")
    case .kyc: return UIImage(named: "settings_kyc")
    case .support: return UIImage(named: "settings_support")
    case .about: return UIImage(named: "settings_about")
    }
  }
}

final class SettingsCell: UITableViewCell {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    stackView.alignment = .center
    stackView.spacing = 15
    return stackView
  }()
  
  let iconImageView = UIImageView(image: nil)
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 16, weight: .medium)
    label.textColor = .slateGrey
    return label
  }()
  
  let valueLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 16, weight: .medium)
    label.textColor = .warmGrey
    return label
  }()
  
  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  override func prepareForReuse() {
    super.prepareForReuse()
    iconImageView.image = nil
    titleLabel.text = nil
    valueLabel.text = nil
  }
  
  private func setupUI() {
    contentView.addSubviews(stackView,
                            valueLabel)
    stackView.addArrangedSubviews(iconImageView,
                                  titleLabel)
  }
  
  private func setupLayout() {
    iconImageView.snp.makeConstraints {
      $0.size.equalTo(24)
    }
    stackView.snp.makeConstraints {
      $0.left.equalToSuperview().offset(15)
      $0.centerY.equalToSuperview()
    }
    valueLabel.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-15)
      $0.centerY.equalToSuperview()
    }
  }
  
  func configure(for type: SettingsCellTypeRepresentable) {
    if type is SupportCellType {
        accessoryType = .disclosureIndicator
    }
    iconImageView.image = type.image
    iconImageView.isHidden = type.image == nil
    iconImageView.contentMode = .center
    titleLabel.text = type.title
    valueLabel.text = type.value
    valueLabel.isHidden = type.value == nil
    isUserInteractionEnabled = type.isEnabled
  }
}

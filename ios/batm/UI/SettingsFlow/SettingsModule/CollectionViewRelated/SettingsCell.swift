import UIKit
import RxSwift
import RxCocoa

enum SettingsCellType: CaseIterable {
  case phone
  case changePassword
  case changePin
  case verification
  case showSeedPhrase
  case unlink
  
  var verboseValue: String {
    switch self {
    case .phone: return localize(L.Settings.changePhone)
    case .changePassword: return localize(L.Settings.changePassword)
    case .changePin: return localize(L.Settings.changePin)
    case .verification: return localize(L.Settings.verification)
    case .showSeedPhrase: return localize(L.Settings.showSeedPhrase)
    case .unlink: return localize(L.Settings.unlinkWallet)
    }
  }
  
  var image: UIImage? {
    switch self {
    case .phone: return UIImage(named: "settings_phone")
    case .changePassword: return UIImage(named: "settings_password")
    case .changePin: return UIImage(named: "settings_pin")
    case .verification: return UIImage(named: "settings_verification")
    case .showSeedPhrase: return UIImage(named: "settings_seed_phrase")
    case .unlink: return UIImage(named: "settings_unlink")
    }
  }
}

final class SettingsCell: UICollectionViewCell {
  
  var disposeBag = DisposeBag()
  
  let typeView: UIView = {
    let view = UIView()
    view.backgroundColor = .ceruleanBlue
    view.layer.cornerRadius = 8
    return view
  }()
  
  let typeImageView = UIImageView(image: nil)
  
  let typeLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsMedium14
    label.textColor = .slateGrey
    return label
  }()
  
  let disclosureImageView = UIImageView(image: UIImage(named: "disclosure_indicator"))
  
  let divider: UIView = {
    let view = UIView()
    view.backgroundColor = UIColor.black.withAlphaComponent(0.1)
    return view
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  override func prepareForReuse() {
    super.prepareForReuse()
    typeImageView.image = nil
    typeLabel.text = nil
    disposeBag = DisposeBag()
  }
  
  private func setupUI() {
    contentView.backgroundColor = .clear
    
    contentView.addSubviews(typeView,
                            typeLabel,
                            disclosureImageView,
                            divider)
    typeView.addSubview(typeImageView)
  }
  
  private func setupLayout() {
    typeView.snp.makeConstraints {
      $0.left.equalToSuperview().offset(10)
      $0.top.bottom.equalToSuperview().inset(14)
      $0.width.equalTo(typeView.snp.height)
    }
    typeImageView.snp.makeConstraints {
      $0.center.equalToSuperview()
    }
    typeLabel.snp.makeConstraints {
      $0.left.equalTo(typeView.snp.right).offset(21)
      $0.centerY.equalToSuperview()
    }
    disclosureImageView.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-10)
      $0.centerY.equalToSuperview()
    }
    divider.snp.makeConstraints {
      $0.left.right.bottom.equalToSuperview()
      $0.height.equalTo(1)
    }
  }
  
  func configure(for type: SettingsCellType) {
    typeImageView.image = type.image
    typeLabel.text = type.verboseValue
  }
}

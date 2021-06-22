import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

protocol ManageWalletsCellDelegate: AnyObject {
  func didTapChangeVisibility(cell: ManageWalletsCell)
}

final class ManageWalletsCell: UITableViewCell {
  
  var disposeBag = DisposeBag()
  
  weak var delegate: ManageWalletsCellDelegate?
  
  let typeImageView = UIImageView(image: nil)
  
  let typeLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 16, weight: .bold)
    label.textColor = .slateGrey
    return label
  }()
  
  let visibilitySwitch: UISwitch = {
    let toggle = UISwitch()
    toggle.onTintColor = .ceruleanBlue
    return toggle
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
    typeImageView.image = nil
    typeLabel.text = nil
    disposeBag = DisposeBag()
  }
  
  private func setupUI() {
    contentView.addSubviews(typeImageView,
                            typeLabel,
                            visibilitySwitch)
  }
  
  private func setupLayout() {
    typeImageView.snp.makeConstraints {
      $0.left.equalToSuperview().offset(15)
      $0.centerY.equalToSuperview()
    }
    typeLabel.snp.makeConstraints {
      $0.left.equalTo(typeImageView.snp.right).offset(15)
      $0.right.lessThanOrEqualTo(visibilitySwitch.snp.left).offset(-15)
      $0.centerY.equalToSuperview()
    }
    visibilitySwitch.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-15)
      $0.centerY.equalToSuperview()
    }
  }
  
  
  func configure(for coin: BTMCoin) {
    typeImageView.image = coin.type.mediumLogo
    typeLabel.text = coin.type.verboseValue
    visibilitySwitch.isOn = coin.isVisible
    visibilitySwitch.addTarget(self, action: #selector(didChangeSwitch), for: .valueChanged)
  }
  
  @objc func didChangeSwitch() {
    delegate?.didTapChangeVisibility(cell: self)
  }
}

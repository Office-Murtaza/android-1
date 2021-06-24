import UIKit
import RxSwift
import RxCocoa

final class WalletHeaderView: UIView {
  
  let logoImageView = UIImageView(image: UIImage(named: "logo"))
  
  let textStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 6
    stackView.alignment = .trailing
    return stackView
  }()
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14, weight: .medium)
    label.textColor = .slateGrey
    label.text = localize(L.Wallet.Header.title)
    return label
  }()
  
  let valueLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 22, weight: .bold)
    label.textColor = .ceruleanBlue
    return label
  }()
  
  let divider = UIView()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    backgroundColor = .white
    
    addSubviews(logoImageView,
                textStackView,
                divider)
    textStackView.addArrangedSubviews(titleLabel,
                                      valueLabel)
  }
  
  private func setupLayout() {
    logoImageView.snp.makeConstraints {
      $0.left.equalToSuperview().offset(15)
      $0.centerY.equalToSuperview()
      $0.keepRatio(for: logoImageView)
    }
    logoImageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
    logoImageView.setContentCompressionResistancePriority(.defaultLow, for: .vertical)
    
    textStackView.snp.makeConstraints {
      $0.left.greaterThanOrEqualTo(logoImageView.snp.right).offset(15)
      $0.right.equalToSuperview().offset(-15)
      $0.centerY.equalToSuperview()
    }
    
    divider.snp.makeConstraints {
      $0.left.right.bottom.equalToSuperview()
      $0.height.equalTo(1 / UIScreen.main.scale)
    }
  }
  
  func configure(for coinsBalance: CoinsBalance) {
    valueLabel.text = coinsBalance.totalBalance?.fiatFormatted.withDollarSign
  }
}

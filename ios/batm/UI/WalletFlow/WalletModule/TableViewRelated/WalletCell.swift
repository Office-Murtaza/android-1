import UIKit

final class WalletCell: UITableViewCell {
  
  let typeImageView = UIImageView(image: nil)
  
  let typeStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 7
    stackView.alignment = .leading
    return stackView
  }()
  
  let valueStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 7
    stackView.alignment = .trailing
    return stackView
  }()
  
  let typeLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 16, weight: .bold)
    label.textColor = .slateGrey
    return label
  }()
  
  let priceLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14)
    label.textColor = .warmGrey
    return label
  }()
  
  let coinBalanceLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 16, weight: .bold)
    label.textColor = .slateGrey
    return label
  }()
  
  let fiatBalanceLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14, weight: .medium)
    label.textColor = .ceruleanBlue
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
    typeImageView.image = nil
    typeLabel.text = nil
    priceLabel.text = nil
    coinBalanceLabel.text = nil
    fiatBalanceLabel.text = nil
  }
  
  private func setupUI() {
    contentView.addSubviews(typeImageView,
                            typeStackView,
                            valueStackView)
    typeStackView.addArrangedSubviews(typeLabel,
                                      priceLabel)
    valueStackView.addArrangedSubviews(coinBalanceLabel,
                                       fiatBalanceLabel)
  }
  
  private func setupLayout() {
    typeImageView.snp.makeConstraints {
      $0.left.equalToSuperview().offset(15)
      $0.centerY.equalToSuperview()
    }
    typeStackView.snp.makeConstraints {
      $0.left.equalTo(typeImageView.snp.right).offset(20)
      $0.centerY.equalToSuperview()
    }
    valueStackView.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-15)
      $0.centerY.equalToSuperview()
    }
  }
  
  func configure(for model: CoinBalance) {
    typeImageView.image = model.type.logo
    typeLabel.text = model.type.verboseValue
    priceLabel.text = model.price.fiatFormatted.withDollarSign
    coinBalanceLabel.text = model.balance.coinFormatted.withCoinType(model.type)
    fiatBalanceLabel.text = model.fiatBalance.fiatFormatted.withDollarSign
  }
}

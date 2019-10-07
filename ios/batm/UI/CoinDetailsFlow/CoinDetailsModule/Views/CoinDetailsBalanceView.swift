import UIKit

class CoinDetailsBalanceView: RoundedView {
  
  let container = UIView()
  
  let priceLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.price)
    label.textColor = .slateGrey
    label.font = .poppinsBold16
    return label
  }()
  
  let priceValueLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .poppinsSemibold14
    return label
  }()
  
  let balanceLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.balance)
    label.textColor = .slateGrey
    label.font = .poppinsBold16
    return label
  }()
  
  let balanceValueContainer = UIView()
  
  let balanceCoinValueLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .poppinsSemibold14
    return label
  }()
  
  let exchangeImageView = UIImageView(image: UIImage(named: "exchange"))
  
  let balanceCurrencyValueLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .poppinsSemibold14
    return label
  }()
  
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
    
    addSubviews(container)
    container.addSubviews(priceLabel,
                          priceValueLabel,
                          balanceLabel,
                          balanceValueContainer)
    balanceValueContainer.addSubviews(balanceCoinValueLabel,
                                      exchangeImageView,
                                      balanceCurrencyValueLabel)
  }
  
  private func setupLayout() {
    container.snp.makeConstraints {
      $0.top.bottom.equalToSuperview().inset(25)
      $0.left.right.equalToSuperview()
    }
    priceLabel.snp.makeConstraints {
      $0.top.centerX.equalToSuperview()
    }
    priceValueLabel.snp.makeConstraints {
      $0.top.equalTo(priceLabel.snp.bottom).offset(10)
      $0.centerX.equalToSuperview()
    }
    balanceLabel.snp.makeConstraints {
      $0.top.equalTo(priceValueLabel.snp.bottom).offset(20)
      $0.centerX.equalToSuperview()
    }
    balanceValueContainer.snp.makeConstraints {
      $0.top.equalTo(balanceLabel.snp.bottom).offset(10)
      $0.centerX.bottom.equalToSuperview()
    }
    balanceCoinValueLabel.snp.makeConstraints {
      $0.left.centerY.equalToSuperview()
    }
    exchangeImageView.snp.makeConstraints {
      $0.left.equalTo(balanceCoinValueLabel.snp.right).offset(15)
      $0.top.bottom.equalToSuperview()
    }
    balanceCurrencyValueLabel.snp.makeConstraints {
      $0.left.equalTo(exchangeImageView.snp.right).offset(15)
      $0.right.centerY.equalToSuperview()
    }
  }
  
  func configure(for coinBalance: CoinBalance) {
    priceValueLabel.text = "\(coinBalance.price.fiatFormatted) USD"
    balanceCoinValueLabel.text = "\(coinBalance.balance.coinFormatted) \(coinBalance.type.code)"
    balanceCurrencyValueLabel.text = "\((coinBalance.balance * coinBalance.price).fiatFormatted) USD"
  }
}


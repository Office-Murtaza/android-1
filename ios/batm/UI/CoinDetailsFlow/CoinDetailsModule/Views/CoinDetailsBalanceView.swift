import UIKit

final class CoinDetailsBalanceView: UIView {
  
  let balanceLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.balance)
    label.textColor = .slateGrey
    label.font = .poppinsSemibold15
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.5
    return label
  }()
  
  let balanceCoinLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .poppinsSemibold15
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.5
    return label
  }()
  
  let exchangeImageView = UIImageView(image: UIImage(named: "exchange"))
  
  let balanceCurrencyLabel: UILabel = {
    let label = UILabel()
    label.textColor = .ceruleanBlue
    label.font = .poppinsSemibold15
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.5
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
  
  override func layoutSubviews() {
    super.layoutSubviews()
    
    if !balanceLabel.adjustsFontSizeToFitWidth { return }
    
    let labels = [balanceLabel, balanceCoinLabel, balanceCurrencyLabel]
    let fontSizes = labels.map { $0.actualFontSize }
    let minimumFontSize = fontSizes.min()!
    let maximumFontSize = fontSizes.max()!
    
    if minimumFontSize < maximumFontSize {
      labels.forEach {
        $0.font = $0.font.withSize(12)
        $0.adjustsFontSizeToFitWidth = false
      }
    }
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(balanceLabel,
                balanceCoinLabel,
                exchangeImageView,
                balanceCurrencyLabel)
  }
  
  private func setupLayout() {
    balanceLabel.snp.makeConstraints {
      $0.left.centerY.equalToSuperview()
    }
    balanceCoinLabel.snp.makeConstraints {
      $0.left.equalTo(balanceLabel.snp.right).offset(10)
      $0.centerY.equalToSuperview()
    }
    exchangeImageView.snp.makeConstraints {
      $0.left.equalTo(balanceCoinLabel.snp.right).offset(10)
      $0.top.bottom.equalToSuperview()
    }
    balanceCurrencyLabel.snp.makeConstraints {
      $0.left.equalTo(exchangeImageView.snp.right).offset(10)
      $0.centerY.right.equalToSuperview()
      $0.height.equalTo(balanceCoinLabel)
    }
  }
  
  func configure(for coinBalance: CoinBalance) {
    balanceCoinLabel.text = "\(coinBalance.balance.coinFormatted) \(coinBalance.type.code)"
    balanceCurrencyLabel.text = "\((coinBalance.balance * coinBalance.price).fiatFormatted) USD"
  }
}

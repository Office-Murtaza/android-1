import UIKit

final class CoinDetailsBalanceValueView: UIView {
  
  let balanceCoinLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.5
    return label
  }()
  
  let exchangeImageView = UIImageView(image: UIImage(named: "exchange"))
  
  let balanceCurrencyLabel: UILabel = {
    let label = UILabel()
    label.textColor = .ceruleanBlue
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
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
    
    if !balanceCoinLabel.adjustsFontSizeToFitWidth { return }
    
    let labels = [balanceCoinLabel, balanceCurrencyLabel]
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
    
    addSubviews(balanceCoinLabel,
                exchangeImageView,
                balanceCurrencyLabel)
  }
  
  private func setupLayout() {
    balanceCoinLabel.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    exchangeImageView.snp.makeConstraints {
      $0.left.equalTo(balanceCoinLabel.snp.right).offset(10)
      $0.centerY.equalToSuperview()
    }
    balanceCurrencyLabel.snp.makeConstraints {
      $0.left.equalTo(exchangeImageView.snp.right).offset(10)
      $0.top.right.bottom.equalToSuperview()
      $0.height.equalTo(balanceCoinLabel)
    }
  }
  
  func configure(cryptoAmount: Double, fiatAmount: Double, type: CustomCoinType) {
    balanceCoinLabel.text = cryptoAmount.coinFormatted.withCoinType(type)
    balanceCurrencyLabel.text = fiatAmount.fiatFormatted.withDollarSign
  }
  
  func configure(for coinBalance: CoinBalance, useReserved: Bool = false) {
    let cryptoAmount = useReserved ? coinBalance.reservedBalance : coinBalance.balance
    let fiatAmount = cryptoAmount * coinBalance.price
    
    configure(cryptoAmount: cryptoAmount, fiatAmount: fiatAmount, type: coinBalance.type)
  }
}

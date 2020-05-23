import UIKit
import RxSwift
import RxCocoa

class CoinWithdrawHeaderView: UIView {
  
  static var defaultVerticalStackView: UIStackView {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 10
    return stackView
  }
  
  static func defaultTitleLabel(_ title: String) -> UILabel {
    let label = UILabel()
    label.text = title
    label.textColor = .slateGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }
  
  static var defaultValueLabel: UILabel {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }
  
  let mainStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    stackView.spacing = 20
    return stackView
  }()
  
  let titleStackView = defaultVerticalStackView
  let valueStackView = defaultVerticalStackView
  
  let priceTitleLabel = defaultTitleLabel(localize(L.CoinDetails.price))
  let priceValueLabel = defaultValueLabel
  
  let balanceTitleLabel = defaultTitleLabel(localize(L.CoinDetails.balance))
  let balanceValueView = CoinDetailsBalanceValueView()
  
  let dailyLimitTitleLabel = defaultTitleLabel(localize(L.CoinSell.dailyLimit))
  let dailyLimitValueLabel = defaultValueLabel
  
  let txLimitTitleLabel = defaultTitleLabel(localize(L.CoinSell.txLimit))
  let txLimitValueLabel = defaultValueLabel
  
  let reservedTitleLabel = defaultTitleLabel(localize(L.Trades.reserved))
  let reservedValueView = CoinDetailsBalanceValueView()

  
  var sellDetailsLabels: [UILabel] {
    return [dailyLimitTitleLabel, dailyLimitValueLabel, txLimitTitleLabel, txLimitValueLabel]
  }
  
  var reservedViews: [UIView] {
    return [reservedTitleLabel, reservedValueView]
  }
  
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
    
    addSubview(mainStackView)
    
    mainStackView.addArrangedSubviews(titleStackView,
                                      valueStackView)
    
    titleStackView.addArrangedSubviews(priceTitleLabel,
                                       balanceTitleLabel,
                                       dailyLimitTitleLabel,
                                       txLimitTitleLabel,
                                       reservedTitleLabel)
    
    valueStackView.addArrangedSubviews(priceValueLabel,
                                       balanceValueView,
                                       dailyLimitValueLabel,
                                       txLimitValueLabel,
                                       reservedValueView)
    
    sellDetailsLabels.forEach { $0.isHidden = true }
    reservedViews.forEach { $0.isHidden = true }
  }
  
  private func setupLayout() {
    mainStackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    
    priceValueLabel.setContentHuggingPriority(.required, for: .vertical)
    dailyLimitValueLabel.setContentHuggingPriority(.required, for: .vertical)
    txLimitValueLabel.setContentHuggingPriority(.required, for: .vertical)
  }
  
  func configure(for coinBalance: CoinBalance, useReserved: Bool = false) {
    priceValueLabel.text = "\(coinBalance.price.fiatFormatted) USD"
    balanceValueView.configure(for: coinBalance)
    
    if useReserved {
      reservedViews.forEach { $0.isHidden = false }
      reservedValueView.configure(for: coinBalance, useReserved: true)
    }
  }
  
  func configure(for details: SellDetails) {
    sellDetailsLabels.forEach { $0.isHidden = false }
    
    dailyLimitValueLabel.text = "\(details.dailyLimit.fiatFormatted) USD"
    txLimitValueLabel.text = "\(details.transactionLimit.fiatFormatted) USD"
  }
}

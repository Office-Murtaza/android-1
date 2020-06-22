import UIKit
import RxSwift
import RxCocoa

class CreateEditTradeHeaderView: UIView {
  
  static var defaultVerticalStackView: UIStackView {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.alignment = .leading
    stackView.spacing = 15
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
  
  let priceTitleView = UIView()
  let priceTitleLabel = defaultTitleLabel(localize(L.CoinDetails.price))
  let priceValueLabel = defaultValueLabel
  
  let balanceTitleView = UIView()
  let balanceTitleLabel = defaultTitleLabel(localize(L.CoinDetails.balance))
  let balanceValueView = CoinDetailsBalanceValueView()
  
  let reservedTitleView = UIView()
  let reservedTitleLabel = defaultTitleLabel(localize(L.Trades.reserved))
  let reservedValueView = CoinDetailsBalanceValueView()
  
  let typeTitleView = UIView()
  let typeTitleLabel = defaultTitleLabel(localize(L.CreateEditTrade.type))
  let typeValueView = CreateEditTradeTypeView()
  
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
    
    priceTitleView.addSubview(priceTitleLabel)
    balanceTitleView.addSubview(balanceTitleLabel)
    reservedTitleView.addSubview(reservedTitleLabel)
    typeTitleView.addSubview(typeTitleLabel)
    
    titleStackView.addArrangedSubviews(priceTitleView,
                                       balanceTitleView,
                                       reservedTitleView,
                                       typeTitleView)
    
    valueStackView.addArrangedSubviews(priceValueLabel,
                                       balanceValueView,
                                       reservedValueView,
                                       typeValueView)
  }
  
  private func setupLayout() {
    mainStackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    
    [priceTitleLabel,
     balanceTitleLabel,
     reservedTitleLabel,
     typeTitleLabel].forEach {
      $0.snp.makeConstraints {
        $0.top.left.right.equalToSuperview()
      }
    }
    
    titleStackView.arrangedSubviews.enumerated().forEach { index, subview in
      subview.snp.makeConstraints {
        $0.height.equalTo(valueStackView.arrangedSubviews[index])
      }
    }
  }
  
  func configure(for coinBalance: CoinBalance, trade: BuySellTrade? = nil) {
    priceValueLabel.text = coinBalance.price.fiatFormatted.withUSD
    balanceValueView.configure(for: coinBalance)
    reservedValueView.configure(for: coinBalance, useReserved: true)
    trade.flatMap { typeValueView.configure(for: $0) }
  }
}

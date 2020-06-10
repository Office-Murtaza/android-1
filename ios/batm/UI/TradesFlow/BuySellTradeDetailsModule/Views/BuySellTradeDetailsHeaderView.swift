import UIKit
import RxSwift
import RxCocoa

class BuySellTradeDetailsHeaderView: UIView {
  
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
  let priceTitleLabel = defaultTitleLabel(localize(L.BuySellTradeDetails.Header.price))
  let priceValueLabel: UILabel = {
    let label = defaultValueLabel
    label.textColor = .darkMint
    return label
  }()
  
  let userTitleView = UIView()
  let userTitleLabel = defaultTitleLabel(localize(L.BuySellTradeDetails.Header.user))
  let userValueLabel: UILabel = {
    let label = defaultValueLabel
    label.numberOfLines = 0
    return label
  }()
  
  let paymentTitleView = UIView()
  let paymentTitleLabel = defaultTitleLabel(localize(L.BuySellTradeDetails.Header.payment))
  let paymentValueLabel = BuySellTradeDetailsPaymentView()
  
  let limitsTitleView = UIView()
  let limitsTitleLabel = defaultTitleLabel(localize(L.BuySellTradeDetails.Header.limits))
  let limitsValueLabel = defaultValueLabel
  
  let termsTitleView = UIView()
  let termsTitleLabel = defaultTitleLabel(localize(L.BuySellTradeDetails.Header.terms))
  let termsValueLabel: UILabel = {
    let label = defaultValueLabel
    label.numberOfLines = 0
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
    
    addSubview(mainStackView)
    
    mainStackView.addArrangedSubviews(titleStackView,
                                      valueStackView)
    
    priceTitleView.addSubview(priceTitleLabel)
    userTitleView.addSubview(userTitleLabel)
    paymentTitleView.addSubview(paymentTitleLabel)
    limitsTitleView.addSubview(limitsTitleLabel)
    termsTitleView.addSubview(termsTitleLabel)
    
    titleStackView.addArrangedSubviews(priceTitleView,
                                       userTitleView,
                                       paymentTitleView,
                                       limitsTitleView,
                                       termsTitleView)
    
    valueStackView.addArrangedSubviews(priceValueLabel,
                                       userValueLabel,
                                       paymentValueLabel,
                                       limitsValueLabel,
                                       termsValueLabel)
  }
  
  private func setupLayout() {
    mainStackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    
    [priceTitleLabel,
     userTitleLabel,
     paymentTitleLabel,
     limitsTitleLabel,
     termsTitleLabel].forEach {
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
  
  func configure(for trade: BuySellTrade) {
    priceValueLabel.text = trade.price.fiatFormatted.withUSD
    userValueLabel.text = "\(trade.username)\n\(trade.userStats)"
    paymentValueLabel.configure(for: trade.paymentMethod)
    limitsValueLabel.text = trade.formattedLimits
    termsValueLabel.text = trade.terms
  }
}

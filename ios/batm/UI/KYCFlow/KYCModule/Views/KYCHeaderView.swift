import UIKit
import RxSwift
import RxCocoa

class KYCHeaderView: UIView {
  
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
  
  let statusTitleView = UIView()
  let statusTitleLabel = defaultTitleLabel(localize(L.KYC.Header.Status.title))
  let statusValueLabel = KYCStatusView()
  
  let transactionLimitTitleView = UIView()
  let transactionLimitTitleLabel = defaultTitleLabel(localize(L.KYC.Header.TransactionLimit.title))
  let transactionLimitValueLabel = defaultValueLabel
  
  let dailyLimitTitleView = UIView()
  let dailyLimitTitleLabel = defaultTitleLabel(localize(L.KYC.Header.DailyLimit.title))
  let dailyLimitValueLabel = defaultValueLabel
  
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
    
    statusTitleView.addSubview(statusTitleLabel)
    transactionLimitTitleView.addSubview(transactionLimitTitleLabel)
    dailyLimitTitleView.addSubview(dailyLimitTitleLabel)
    
    titleStackView.addArrangedSubviews(statusTitleView,
                                       transactionLimitTitleView,
                                       dailyLimitTitleView)
    
    valueStackView.addArrangedSubviews(statusValueLabel,
                                       transactionLimitValueLabel,
                                       dailyLimitValueLabel)
  }
  
  private func setupLayout() {
    mainStackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    
    [statusTitleLabel,
     transactionLimitTitleLabel,
     dailyLimitTitleLabel].forEach {
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
  
  func configure(for kyc: KYC) {
    statusValueLabel.configure(for: kyc.status)
    transactionLimitValueLabel.text = kyc.txLimit.fiatSellFormatted.withUSD
    dailyLimitValueLabel.text = kyc.dailyLimit.fiatSellFormatted.withUSD
  }
}

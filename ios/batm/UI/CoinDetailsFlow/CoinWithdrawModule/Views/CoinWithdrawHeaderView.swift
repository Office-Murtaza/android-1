import UIKit
import RxSwift
import RxCocoa

class CoinWithdrawHeaderView: UIView {
  
  let mainStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    stackView.spacing = 20
    return stackView
  }()
  
  let titleStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 10
    return stackView
  }()
  
  let valueStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 10
    return stackView
  }()
  
  let priceLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.price)
    label.textColor = .slateGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }()
  
  let priceValueLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }()
  
  let balanceLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.balance)
    label.textColor = .slateGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }()
  
  let balanceValueView = CoinDetailsBalanceValueView()
  
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
    
    titleStackView.addArrangedSubviews(priceLabel,
                                       balanceLabel)
    
    valueStackView.addArrangedSubviews(priceValueLabel,
                                       balanceValueView)
  }
  
  private func setupLayout() {
    mainStackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
    
    priceValueLabel.setContentHuggingPriority(.required, for: .vertical)
  }
  
  func configure(for coinBalance: CoinBalance) {
    priceValueLabel.text = "\(coinBalance.price.fiatFormatted) USD"
    balanceValueView.configure(for: coinBalance)
  }
}

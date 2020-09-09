import UIKit

final class CoinDetailsBalanceView: UIView {
  
  let balanceLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.balance)
    label.textColor = .warmGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }()
  
  let amountView = CryptoFiatAmountView()
  
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
    
    addSubviews(balanceLabel, amountView)
  }
  
  private func setupLayout() {
    balanceLabel.snp.makeConstraints {
      $0.left.equalToSuperview().offset(15)
      $0.centerY.equalToSuperview()
    }
    balanceLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
    amountView.snp.makeConstraints {
      $0.left.greaterThanOrEqualTo(balanceLabel.snp.right).offset(15)
      $0.right.equalToSuperview().offset(-15)
      $0.centerY.equalToSuperview()
    }
  }
  
  func configure(for coinBalance: CoinBalance) {
    amountView.configure(for: coinBalance, weighted: true)
  }
}

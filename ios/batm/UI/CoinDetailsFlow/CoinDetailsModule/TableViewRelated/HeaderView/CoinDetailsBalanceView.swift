import UIKit

final class CoinDetailsBalanceView: UIView {
  
  let balanceLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.balance)
    label.textColor = .warmGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }()
    
  let reservedLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinDetails.reserved)
    label.textColor = .warmGrey
    label.font = UIFont.systemFont(ofSize: 16, weight: .medium)
    return label
  }()
    
  let amountView = CryptoFiatAmountView()
  let reservedView = CryptoFiatAmountView()
  
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
    
    addSubviews(balanceLabel, reservedLabel, amountView, reservedView)
  }
  
  private func setupLayout() {
    balanceLabel.snp.makeConstraints {
        $0.left.top.equalToSuperview().offset(15)
    }
    reservedLabel.snp.makeConstraints {
        $0.top.equalTo(balanceLabel.snp.bottom).offset(15)
        $0.bottom.equalToSuperview().offset(-15)
        $0.left.equalToSuperview().offset(15)
    }
    balanceLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
    reservedLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
    amountView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(15)
      $0.left.greaterThanOrEqualTo(balanceLabel.snp.right).offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    reservedView.snp.makeConstraints {
        $0.left.greaterThanOrEqualTo(reservedLabel.snp.right).offset(15)
        $0.bottom.right.equalToSuperview().offset(-15)
        $0.top.equalTo(amountView.snp.bottom)
    }
  }
  
  func configure(for coinBalance: CoinBalance) {
     amountView.configure(for: coinBalance, weighted: true)
    reservedView.configure(for: coinBalance, useReserved: true, weighted: true)
  }
}

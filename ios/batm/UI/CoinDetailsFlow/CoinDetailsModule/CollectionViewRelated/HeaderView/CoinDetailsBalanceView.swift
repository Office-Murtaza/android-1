import UIKit

final class CoinDetailsBalanceView: UIView {
  
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
    
    addSubviews(balanceLabel, balanceValueView)
  }
  
  private func setupLayout() {
    balanceLabel.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    balanceLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
    balanceValueView.snp.makeConstraints {
      $0.left.equalTo(balanceLabel.snp.right).offset(10)
      $0.top.right.bottom.equalToSuperview()
    }
  }
  
  func configure(for coinBalance: CoinBalance) {
    balanceValueView.configure(for: coinBalance)
  }
}

import UIKit
import SnapKit

class P2PTradeDetailsOpenOrdersView: UIView {
  
  private let titleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14, weight: .regular)
    label.textColor = UIColor(hexString: "#58585A")
    return label
  }()
  
  private let valueLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14, weight: .bold)
    label.textColor = .black
    return label
  }()
  
  func setOpenOrdersValue( _ openOrders: Int?) {
    let value = String(openOrders ?? 0)
    valueLabel.text = value
  }
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupLayout()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    
    titleLabel.text = "Open orders"
    valueLabel.text = "0"
    
    addSubviews([
      titleLabel,
      valueLabel
    ])
 
  }
  
  private func setupLayout() {
    titleLabel.snp.makeConstraints {
      $0.left.equalToSuperview().offset(16)
      $0.centerY.equalToSuperview()
    }
    
    valueLabel.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-16)
      $0.centerY.equalToSuperview()
    }
  }
}

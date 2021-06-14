import UIKit
import SnapKit

class P2PCreateOrderInfoLine: UIView {
  lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = UIColor.black.withAlphaComponent(0.6)
    return label
  }()
  
  
  lazy var valueLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = UIColor.black
    label.adjustsFontSizeToFitWidth = true
    return label
  }()
  
  func setup(title: String, value: String) {
    titleLabel.text = title
    valueLabel.text = value
  }
  
  func update(value: String) {
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
    addSubviews([
      titleLabel,
      valueLabel
    ])
  }
  
  private func setupLayout() {
    titleLabel.snp.makeConstraints {
      $0.top.bottom.left.equalToSuperview()
    }
    
    valueLabel.snp.makeConstraints {
      $0.top.right.bottom.equalToSuperview()
      $0.left.greaterThanOrEqualTo(titleLabel.snp.right).offset(10)
    }
  }
  
}

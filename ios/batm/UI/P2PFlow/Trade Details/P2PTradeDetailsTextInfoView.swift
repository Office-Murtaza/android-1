import UIKit
import SnapKit

class P2PTradeDetailsTextInfoView: UIView {
  
  private let notificationLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14, weight: .regular)
    label.textColor = .darkGray
    label.numberOfLines = 0
    return label
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupLayout()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func update(message: String) {
    notificationLabel.text = message
  }
  
  private func setupUI() {
    addSubview(notificationLabel)
  }
  
  private func setupLayout() {
    notificationLabel.snp.makeConstraints {
      $0.top.equalToSuperview().offset(25)
      $0.bottom.equalToSuperview().offset(-25)
      $0.right.equalToSuperview().offset(-15)
      $0.left.equalToSuperview().offset(15)
    }
  }
  
}

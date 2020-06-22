import UIKit
import RxSwift
import RxCocoa

final class BuySellTradeDetailsPaymentView: UIView {
  
  let textLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 11, weight: .bold)
    label.textColor = .white
    label.adjustsFontSizeToFitWidth = true
    label.minimumScaleFactor = 0.7
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
    backgroundColor = .lightGold
    layer.cornerRadius = 3
    
    addSubview(textLabel)
  }
  
  private func setupLayout() {
    textLabel.snp.makeConstraints {
      $0.top.bottom.equalToSuperview().inset(3)
      $0.left.right.equalToSuperview().inset(5)
    }
  }
  
  func configure(for payment: String) {
    textLabel.text = payment
  }
  
  func reset() {
    textLabel.text = nil
  }
}

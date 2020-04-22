import UIKit
import RxSwift
import RxCocoa

final class TransactionStatusView: UIView {
  
  let statusLabel: UILabel = {
    let label = UILabel()
    label.font = .poppinsSemibold10
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
    layer.cornerRadius = 3
    
    addSubview(statusLabel)
  }
  
  private func setupLayout() {
    statusLabel.snp.makeConstraints {
      $0.top.bottom.equalToSuperview().inset(2)
      $0.left.right.equalToSuperview().inset(5)
    }
  }
  
  func configure(for status: TransactionStatus) {
    backgroundColor = status.associatedColor
    statusLabel.text = status.verboseValue
  }
  
  func reset() {
    backgroundColor = nil
    statusLabel.text = nil
  }
}

import UIKit

class KYCStatusView: UIView {
  
  let titleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 11, weight: .bold)
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
    translatesAutoresizingMaskIntoConstraints = false
    
    layer.borderWidth = 1
    layer.cornerRadius = 3
    
    addSubview(titleLabel)
  }
  
  private func setupLayout() {
    titleLabel.snp.makeConstraints {
      $0.top.bottom.equalToSuperview().inset(4)
      $0.left.right.equalToSuperview().inset(5)
    }
  }
  
  func configure(for status: KYCStatus) {
    titleLabel.text = status.verboseValue
    titleLabel.textColor = status.associatedColor
    layer.borderColor = status.associatedColor.cgColor
    backgroundColor = status.associatedColor.withAlphaComponent(0.1)
  }
}

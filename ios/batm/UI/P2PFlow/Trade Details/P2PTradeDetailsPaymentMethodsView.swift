import UIKit
import SnapKit

class P2PTradeDetailsPaymentMethodsView: UIView {
  
  private let titleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 14, weight: .regular)
    label.textColor = UIColor(hexString: "#58585A")
    return label
  }()
  
  private lazy var paymentMethodsView: UIStackView = {
      let stack = UIStackView()
      addSubview(stack)
      stack.axis = .horizontal
      stack.distribution = .fillProportionally
      stack.spacing = 5
      return stack
  }()
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupLayout()
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func update(images: [UIImage]?) {
    guard let images = images else { return }
    let imageViews = images.map { UIImageView(image: $0) }
    paymentMethodsView.addArrangedSubviews(imageViews)
  }
  
  private func setupUI() {
    
    titleLabel.text = "Payment Methods"
    
    addSubviews([
      titleLabel,
      paymentMethodsView
    ])
  }
  
  private func setupLayout() {
    titleLabel.snp.makeConstraints {
      $0.left.equalToSuperview().offset(16)
      $0.centerY.equalToSuperview()
    }
    
    paymentMethodsView.snp.makeConstraints {
      $0.right.equalToSuperview().offset(-16)
      $0.centerY.equalToSuperview()
    }
  }
}

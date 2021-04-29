import UIKit
import SnapKit
import MaterialComponents

class P2PCreateOrderPopupViewController: UIViewController {
  
  private let containerView = UIView()
  private let orderAmountView = P2PCreateOrderAmountView()
  
  
  private let platformFeeTitleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    return label
  }()
  
  private let platformFeeValueLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    return label
  }()
  
  private let platformFeeStackView: UIStackView = {
    let stack = UIStackView()
    stack.distribution = .equalCentering
    stack.axis = .horizontal
    return stack
  }()
  
  private let submitButton = MDCButton.submit
  
  override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = UIColor.black.withAlphaComponent(0.3)
    orderAmountView.fiatTextField.becomeFirstResponder()
    setupUI()
    setupLayout()
    setupRecognizers()
  }
  
  func setupRecognizers() {
    let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(hideController))
    let swipeRecogizer = UISwipeGestureRecognizer(target: self, action: #selector(hideController))
    swipeRecogizer.direction = .down
    
    view.addGestureRecognizer(tapRecognizer)
    view.addGestureRecognizer(swipeRecogizer)
  }
  
  @objc func hideController() {
    dismiss(animated: true, completion: nil)
  }
  
  func setupUI() {
    containerView.backgroundColor = .white
    view.addSubviews([
      containerView
    ])
    
    containerView.addSubviews([
      orderAmountView,
      platformFeeStackView,
      submitButton
    ])
    
    platformFeeStackView.addArrangedSubviews([
      platformFeeTitleLabel,
      platformFeeValueLabel
    ])
    
    platformFeeTitleLabel.text = "Platform fee 3% ~"
    platformFeeValueLabel.text = "0"
    
  }
  
  func setupLayout() {
    orderAmountView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(100)
    }
    
    containerView.snp.makeConstraints {
      $0.left.right.equalToSuperview()
      $0.bottom.equalToSuperview().offset(-300)
      $0.height.equalTo(236)
    }
    
    platformFeeStackView.snp.makeConstraints {
      $0.top.equalTo(orderAmountView.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
    }
    
    submitButton.snp.makeConstraints {
      $0.top.equalTo(platformFeeStackView.snp.bottom).offset(25)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
      $0.height.equalTo(50)
    }
    
  }
  
}

import UIKit
import SnapKit

class P2PCreateOrderPopupViewController: UIViewController {
  
  private let containerView = UIView()
  private let orderAmountView = P2PCreateOrderAmountView()
  
  override func viewDidLoad() {
    super.viewDidLoad()
    view.backgroundColor = UIColor.black.withAlphaComponent(0.3)

    setupUI()
    setupLayout()
  }
  
  func setupUI() {
    containerView.backgroundColor = .white
    view.addSubview(containerView)
    containerView.addSubviews([
      orderAmountView
    ])
  }
  
  func setupLayout() {
    orderAmountView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(100)
    }
    
    containerView.snp.makeConstraints {
      $0.left.right.equalToSuperview()
      $0.bottom.equalToSuperview()
      $0.height.equalTo(236)
    }
  }
  
}

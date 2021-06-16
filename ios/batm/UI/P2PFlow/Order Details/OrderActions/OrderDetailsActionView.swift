import UIKit
import MaterialComponents
import SnapKit

protocol OrderDetailsActionViewDelegate: AnyObject {
  func didTap(type: OrderDetailsActionType)
}

class OrderDetailsActionView: UIView {
  
  weak var delegate: OrderDetailsActionViewDelegate?
  var currentActionType: OrderDetailsActionType = .none
  
  func update(type: OrderDetailsActionType) {
    
    currentActionType = type
    subviews.forEach { $0.removeFromSuperview() }
    guard let renderedButton = renderButton(type) else { return }
    addSubview(renderedButton)

    renderedButton.addTarget(self, action: #selector(didTap), for: .touchUpInside)
    
    renderedButton.snp.makeConstraints {
      $0.top.bottom.equalToSuperview()
      $0.right.equalToSuperview().offset(-15)
      $0.height.equalTo(50)
      $0.left.equalToSuperview().offset(15)
    }
    
  }
  
  func renderButton(_ type: OrderDetailsActionType) -> MDCButton? {
    switch type {
    case .cancel: return MDCButton.cancelLinkButton
    case .doing: return MDCButton.doing
    case .paid: return MDCButton.paid
    case .release: return MDCButton.release
    case .disput: return MDCButton.dispute
    default: return nil
    }
  }
  
  @objc func didTap() {
    delegate?.didTap(type: currentActionType)
  }
  
}




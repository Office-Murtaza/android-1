import UIKit
import SnapKit

protocol OrderDetailsActionSheetDelegate: AnyObject  {
  func didTap(type: OrderDetailsActionType)
}

class OrderDetailsActionSheet: UIView {
  
  weak var delegate: OrderDetailsActionSheetDelegate?
  
  private lazy var stackView: UIStackView = {
    let stack = UIStackView()
    stack.axis = .vertical
    stack.distribution = .fillProportionally
    stack.spacing = 5
    return stack
  }()
  
  required init?(coder: NSCoder) {
      fatalError("init(coder:) has not been implemented")
  }

  override init(frame: CGRect) {
    super.init(frame: frame)
    setupUI()
    setupLayout()
  }
 
  private func setupUI() {
    addSubview(stackView)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  func update(action: OrderDetailsActionViewModel) {
    stackView.arrangedSubviews.forEach{ $0.removeFromSuperview() }
    action.actionViews().forEach { (view) in
      view.delegate = self
      stackView.addArrangedSubview(view)
    }
  }
}

extension OrderDetailsActionSheet: OrderDetailsActionViewDelegate {
  func didTap(type: OrderDetailsActionType) {
    delegate?.didTap(type: type)
  }
}

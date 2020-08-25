import UIKit
import RxSwift
import RxCocoa

class VIPVerificationFormView: UIView {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 15
    return stackView
  }()
  
  let ssnTextField: MainTextField = {
    let textField = MainTextField()
    textField.configure(for: .ssn)
    return textField
  }()
  
  let sendButton: MainButton = {
    let button = MainButton()
    button.configure(for: .send)
    return button
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
    
    addSubview(stackView)
    stackView.addArrangedSubviews(ssnTextField,
                                  sendButton)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
}

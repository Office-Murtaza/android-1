import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

class VIPVerificationFormView: UIView {
    
    let ssnTextField = MDCTextField.zipCode
    
    let ssnTextFieldController: ThemedTextInputControllerOutlined
    
    override init(frame: CGRect) {
        ssnTextFieldController = ThemedTextInputControllerOutlined(textInput: ssnTextField)
        
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        
        addSubview(ssnTextField)
        
        ssnTextFieldController.placeholderText = localize(L.VIPVerification.Form.SSN.placeholder)
    }
    
    private func setupLayout() {
        ssnTextField.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
}

extension Reactive where Base == VIPVerificationFormView {
    var ssnText: ControlProperty<String?> {
        return base.ssnTextField.rx.text
    }
    var ssnErrorText: Binder<String?> {
        return Binder(base) { target, value in
            target.ssnTextFieldController.setErrorText(value, errorAccessibilityValue: value)
        }
    }
}

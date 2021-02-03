import UIKit
import MaterialComponents
import RxSwift
import RxCocoa

class TransferSelectReceiverHeader: UIView {
    
    let phoneNumberTextField: MDCTextField = {
        let field = MDCTextField.phone
        field.rightView = nil
        return field
    }()
    
    let phoneNumberTextFieldController: ThemedTextInputControllerOutlined
    
    override init(frame: CGRect) {
        self.phoneNumberTextFieldController = ThemedTextInputControllerOutlined(textInput: phoneNumberTextField)
        super.init(frame: frame)
        setupUI() 
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupUI() {
        phoneNumberTextFieldController.placeholderText = localize(L.Transfer.Receiver.Phone.placeholder)
    }
    
    func setupLayout() {
        addSubview(phoneNumberTextField)
        phoneNumberTextField.snp.makeConstraints {
            $0.top.equalToSuperview()
            $0.left.equalToSuperview().offset(17)
            $0.right.equalToSuperview().offset(-17)
            $0.bottom.equalToSuperview()
        }
    }
}

extension Reactive where Base == TransferSelectReceiverHeader {
    var text: ControlProperty<String?> {
        return base.phoneNumberTextField.rx.text
    }
    
    var phoneErrorText: Binder<String?> {
        return Binder(base) { target, value in
            target.phoneNumberTextFieldController.setErrorText(value, errorAccessibilityValue: value)
        }
    }
}

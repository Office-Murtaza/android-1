import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class CreateEditTradeFormView: UIView {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let paymentTextField = MDCTextField.default
  let marginTextField = MDCTextField.amount
  let minLimitTextField = MDCTextField.phone
  let maxLimitTextField = MDCTextField.phone
  let termsTextField = MDCMultilineTextField.default
  
  let paymentTextFieldController: ThemedTextInputControllerOutlined
  let marginTextFieldController: ThemedTextInputControllerOutlined
  let minLimitTextFieldController: ThemedTextInputControllerOutlined
  let maxLimitTextFieldController: ThemedTextInputControllerOutlined
  let termsTextFieldController: MDCTextInputControllerOutlinedTextArea
  
  override init(frame: CGRect) {
    paymentTextFieldController = ThemedTextInputControllerOutlined(textInput: paymentTextField)
    marginTextFieldController = ThemedTextInputControllerOutlined(textInput: marginTextField)
    minLimitTextFieldController = ThemedTextInputControllerOutlined(textInput: minLimitTextField)
    maxLimitTextFieldController = ThemedTextInputControllerOutlined(textInput: maxLimitTextField)
    termsTextFieldController = ThemedTextInputControllerOutlinedTextArea(textInput: termsTextField)
    
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(stackView,
                termsTextField)
    stackView.addArrangedSubviews(paymentTextField,
                                  marginTextField,
                                  minLimitTextField,
                                  maxLimitTextField)

    marginTextField.setRightView(UIImageView(image: UIImage(named: "margin")))
    
    paymentTextFieldController.placeholderText = localize(L.CreateEditTrade.Form.Payment.placeholder)
    marginTextFieldController.placeholderText = localize(L.CreateEditTrade.Form.Margin.placeholder)
    minLimitTextFieldController.placeholderText = localize(L.CreateEditTrade.Form.MinLimit.placeholder)
    maxLimitTextFieldController.placeholderText = localize(L.CreateEditTrade.Form.MaxLimit.placeholder)
    termsTextFieldController.placeholderText = localize(L.CreateEditTrade.Form.Terms.placeholder)
    
    termsTextField.minimumLines = 3
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    termsTextField.snp.makeConstraints {
      $0.top.equalTo(stackView.snp.bottom).offset(10)
      $0.left.right.bottom.equalToSuperview()
    }
  }
}

extension Reactive where Base == CreateEditTradeFormView {
  var paymentText: ControlProperty<String?> {
    return base.paymentTextField.rx.text
  }
  var marginText: ControlProperty<String?> {
    return base.marginTextField.rx.text
  }
  var minLimitText: ControlProperty<String?> {
    return base.minLimitTextField.rx.text
  }
  var maxLimitText: ControlProperty<String?> {
    return base.maxLimitTextField.rx.text
  }
  var termsText: ControlProperty<String?> {
    return base.termsTextField.rx.text
  }
}

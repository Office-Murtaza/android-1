import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class CoinSellFormView: UIView {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let currencyMaxButton = MDCButton.max
  
  let coinAmountTextField: MDCTextField = {
    let textField = MDCTextField.amount
    textField.isEnabled = false
    return textField
  }()
  let currencyAmountTextField = MDCTextField.amount
  
  let coinAmountTextFieldController: MDCTextInputControllerOutlined
  let currencyAmountTextFieldController: MDCTextInputControllerOutlined
  
  let anotherAddressView = CoinSellAnotherAddressView()
  
  override init(frame: CGRect) {
    coinAmountTextFieldController = ThemedTextInputControllerOutlined(textInput: coinAmountTextField)
    currencyAmountTextFieldController = ThemedTextInputControllerOutlined(textInput: currencyAmountTextField)
    
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
                anotherAddressView)
    stackView.addArrangedSubviews(coinAmountTextField,
                                  currencyAmountTextField)
    
    currencyAmountTextField.setRightView(currencyMaxButton)
    
    coinAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CoinAmount.placeholder)
    currencyAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CurrencyAmount.placeholder)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    anotherAddressView.snp.makeConstraints {
      $0.top.equalTo(stackView.snp.bottom).offset(10)
      $0.left.equalToSuperview().offset(15)
      $0.bottom.equalToSuperview()
    }
  }
  
  func configure(with coinCode: String) {
    coinAmountTextFieldController.placeholderText = String(format: localize(L.CoinWithdraw.Form.CoinAmount.placeholder), coinCode)
  }
}

extension Reactive where Base == CoinSellFormView {
  var currencyText: ControlProperty<String?> {
    return base.currencyAmountTextField.rx.text
  }
  var coinText: ControlProperty<String?> {
    return base.coinAmountTextField.rx.text
  }
  var maxTap: Driver<Void> {
    return base.currencyMaxButton.rx.tap.asDriver()
  }
  var isAnotherAddress: Driver<Bool> {
    return base.anotherAddressView.rx.isAccepted
  }
}

import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class CoinWithdrawFormView: UIView {
  
  static var maxButton: MDCButton {
    let button = MDCButton()
    button.setTitle(localize(L.CoinWithdraw.Button.max), for: .normal)
    button.setTitleColor(.ceruleanBlue, for: .normal)
    button.contentEdgeInsets = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
    button.setBackgroundColor(.white)
    return button
  }
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let coinMaxButton = maxButton
  
  let currencyMaxButton = maxButton
  
  let pasteButton: MDCButton = {
    let button = MDCButton()
    button.setTitle(localize(L.CoinWithdraw.Button.paste), for: .normal)
    button.setTitleColor(.ceruleanBlue, for: .normal)
    button.contentEdgeInsets = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
    button.setBackgroundColor(.white)
    return button
  }()
  
  let scanButton: MDCButton = {
    let button = MDCButton()
    button.setImage(UIImage(named: "scan"), for: .normal)
    button.contentEdgeInsets = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
    button.setBackgroundColor(.white)
    return button
  }()
  
  let addressButtonsStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .horizontal
    return stackView
  }()
  
  let addressTextField: MDCTextField = {
    let textField = MDCTextField()
    textField.backgroundColor = .white
    return textField
  }()
  
  let coinAmountTextField: MDCTextField = {
    let textField = MDCTextField()
    textField.backgroundColor = .white
    textField.keyboardType = .decimalPad
    return textField
  }()
  
  let currencyAmountTextField: MDCTextField = {
    let textField = MDCTextField()
    textField.backgroundColor = .white
    textField.keyboardType = .decimalPad
    return textField
  }()
  
  let addressTextFieldController: MDCTextInputControllerOutlined
  let coinAmountTextFieldController: MDCTextInputControllerOutlined
  let currencyAmountTextFieldController: MDCTextInputControllerOutlined
  
  override init(frame: CGRect) {
    addressTextFieldController = MDCTextInputControllerOutlined(textInput: addressTextField)
    coinAmountTextFieldController = MDCTextInputControllerOutlined(textInput: coinAmountTextField)
    currencyAmountTextFieldController = MDCTextInputControllerOutlined(textInput: currencyAmountTextField)
    
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
    stackView.addArrangedSubviews(addressTextField,
                                  coinAmountTextField,
                                  currencyAmountTextField)
    
    addressButtonsStackView.addArrangedSubviews(pasteButton,
                                                scanButton)

    addressTextField.rightView = addressButtonsStackView
    addressTextField.rightViewMode = .always

    coinAmountTextField.rightView = coinMaxButton
    coinAmountTextField.rightViewMode = .always
    
    currencyAmountTextField.rightView = currencyMaxButton
    currencyAmountTextField.rightViewMode = .always
    
    let scheme = MDCContainerScheme()
    scheme.colorScheme = MDCSemanticColorScheme(defaults: .material201907)
    scheme.colorScheme.primaryColor = .ceruleanBlue
    scheme.colorScheme.onSurfaceColor = .warmGrey
    
    addressTextFieldController.applyTheme(withScheme: scheme)
    coinAmountTextFieldController.applyTheme(withScheme: scheme)
    currencyAmountTextFieldController.applyTheme(withScheme: scheme)
    
    addressTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.RecipientAddress.placeholder)
    coinAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CoinAmount.placeholder)
    currencyAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CurrencyAmount.placeholder)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  func configure(with coinCode: String) {
    coinAmountTextFieldController.placeholderText = "\(coinCode) \(localize(L.CoinWithdraw.Form.CoinAmount.placeholder))"
  }
}

extension Reactive where Base == CoinWithdrawFormView {
  var currencyText: ControlProperty<String?> {
    return base.currencyAmountTextField.rx.text
  }
  var coinText: ControlProperty<String?> {
    return base.coinAmountTextField.rx.text
  }
  var addressText: ControlProperty<String?> {
    return base.addressTextField.rx.text
  }
  var maxTap: Driver<Void> {
    return Driver.merge(base.coinMaxButton.rx.tap.asDriver(),
                        base.currencyMaxButton.rx.tap.asDriver())
  }
  var pasteTap: Driver<Void> {
    return base.pasteButton.rx.tap.asDriver()
  }
  var scanTap: Driver<Void> {
    return base.scanButton.rx.tap.asDriver()
  }
}

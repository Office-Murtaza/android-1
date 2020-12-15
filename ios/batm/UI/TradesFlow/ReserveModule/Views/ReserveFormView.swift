import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class ReserveFormView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let coinMaxButton = MDCButton.max
  let currencyMaxButton = MDCButton.max
  
  let coinAmountTextField = MDCTextField.amount
  let currencyAmountTextField = MDCTextField.amount
  
  let coinAmountTextFieldController: ThemedTextInputControllerOutlined
  let currencyAmountTextFieldController: ThemedTextInputControllerOutlined
  
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
    
  func configure(coinType: CustomCoinType, fee: Decimal? = nil) {
    coinAmountTextFieldController.placeholderText = String(format: localize(L.CoinWithdraw.Form.CoinAmount.placeholder), coinType.code)
    
    guard let fee = fee else { return }
    let coinType = coinType.isETHBased ? CustomCoinType.ethereum : coinType
    let helperValueText = fee.coinFormatted.withCoinType(coinType)
    let helperText = String(format: localize(L.CoinWithdraw.Form.CoinAmount.helper), helperValueText)
        
    coinAmountTextFieldController.setHelperText(helperText, helperAccessibilityLabel: helperText)
  }
    
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    setupTextFields()
    addSubviews(stackView)
    stackView.addArrangedSubviews(coinAmountTextField,
                                  currencyAmountTextField)

    coinAmountTextField.setRightView(coinMaxButton)
    currencyAmountTextField.setRightView(currencyMaxButton)
    
    coinAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CoinAmount.placeholder)
    currencyAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CurrencyAmount.placeholder)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  func configure(with coinCode: String) {
    coinAmountTextFieldController.placeholderText = String(format: localize(L.CoinWithdraw.Form.CoinAmount.placeholder), coinCode)
  }
    
  private func setupTextFields() {
    let toolbar = UIToolbar()
    let flexSpace = UIBarButtonItem(barButtonSystemItem: .flexibleSpace,
                                    target: nil,
                                    action: nil)
    let doneButton = UIBarButtonItem(title: localize(L.Shared.Button.done),
                                     style: .done,
                                     target: self,
                                     action: #selector(doneButtonTapped))
        
    toolbar.setItems([flexSpace, doneButton], animated: true)
    toolbar.sizeToFit()
        
    coinAmountTextField.inputAccessoryView = toolbar
    currencyAmountTextField.inputAccessoryView = toolbar
  }
    
  @objc private func doneButtonTapped() {
    endEditing(true)
  }
}

extension Reactive where Base == ReserveFormView {
  var currencyText: ControlProperty<String?> {
    return base.currencyAmountTextField.rx.text
  }
  var coinText: ControlProperty<String?> {
    return base.coinAmountTextField.rx.text
  }
  var coinAmountErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.coinAmountTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var maxTap: Driver<Void> {
    return Driver.merge(base.coinMaxButton.rx.tap.asDriver(),
                        base.currencyMaxButton.rx.tap.asDriver())
  }
}

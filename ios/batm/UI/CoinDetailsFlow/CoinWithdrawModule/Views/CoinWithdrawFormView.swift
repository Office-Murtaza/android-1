import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class CoinWithdrawFormView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let addressButtonsStackView = UIStackView()
  
  let coinMaxButton = MDCButton.max
  let currencyMaxButton = MDCButton.max
  let pasteButton = MDCButton.paste
  let scanButton = MDCButton.scan
  
  let addressTextField = MDCOutlinedTextArea.address
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
    setupBindings()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(addressTextField,
                stackView)
    stackView.addArrangedSubviews(coinAmountTextField,
                                  currencyAmountTextField)
    
    addressButtonsStackView.addArrangedSubviews(pasteButton,
                                                scanButton)

    addressTextField.setRightView(addressButtonsStackView)
    coinAmountTextField.setRightView(coinMaxButton)
    currencyAmountTextField.setRightView(currencyMaxButton)
    
    addressTextField.label.text = localize(L.CoinWithdraw.Form.RecipientAddress.placeholder)
    coinAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CoinAmount.placeholder)
    currencyAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CurrencyAmount.placeholder)
  }
  
  private func setupLayout() {
    addressTextField.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    stackView.snp.makeConstraints {
      $0.top.equalTo(addressTextField.snp.bottom).offset(20)
      $0.left.right.bottom.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    addressTextField.rx.text
      .asDriver()
      .filterNil()
      .filterEmpty()
      .distinctUntilChanged()
      .drive(onNext: { [addressTextField] _ in
        if !addressTextField.textView.isFirstResponder {
          addressTextField.textView.becomeFirstResponder()
        }
      })
      .disposed(by: disposeBag)
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

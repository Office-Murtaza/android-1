import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class BuySellTradeDetailsFormView: UIView, HasDisposeBag {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let coinMaxButton = MDCButton.max
  let currencyMaxButton = MDCButton.max
  
  let coinAmountTextField = MDCTextField.amount
  let currencyAmountTextField = MDCTextField.amount
  let requestDetailsTextField = MDCMultilineTextField.default
  
  let coinAmountTextFieldController: ThemedTextInputControllerOutlined
  let currencyAmountTextFieldController: ThemedTextInputControllerOutlined
  let requestDetailsTextFieldController: MDCTextInputControllerOutlinedTextArea
  
  override init(frame: CGRect) {
    coinAmountTextFieldController = ThemedTextInputControllerOutlined(textInput: coinAmountTextField)
    currencyAmountTextFieldController = ThemedTextInputControllerOutlined(textInput: currencyAmountTextField)
    requestDetailsTextFieldController = ThemedTextInputControllerOutlinedTextArea(textInput: requestDetailsTextField)
    
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
    
    addSubviews(stackView,
                requestDetailsTextField)
    stackView.addArrangedSubviews(coinAmountTextField,
                                  currencyAmountTextField)

    coinAmountTextField.setRightView(coinMaxButton)
    currencyAmountTextField.setRightView(currencyMaxButton)
    
    coinAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CoinAmount.placeholder)
    currencyAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CurrencyAmount.placeholder)
    requestDetailsTextFieldController.placeholderText = localize(L.BuySellTradeDetails.Form.RequestDetails.placeholder)
    
    requestDetailsTextFieldController.minimumLines = 3
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    requestDetailsTextField.snp.makeConstraints {
      $0.top.equalTo(stackView.snp.bottom).offset(10)
      $0.left.right.bottom.equalToSuperview()
    }
  }
  
  private func setupBindings() {
  }
  
  func configure(with coinCode: String) {
    coinAmountTextFieldController.placeholderText = String(format: localize(L.CoinWithdraw.Form.CoinAmount.placeholder), coinCode)
  }
}

extension Reactive where Base == BuySellTradeDetailsFormView {
  var currencyText: ControlProperty<String?> {
    return base.currencyAmountTextField.rx.text
  }
  var coinText: ControlProperty<String?> {
    return base.coinAmountTextField.rx.text
  }
  var requestDetailsText: Driver<String?> {
    return base.requestDetailsTextField.rx.text.asDriver()
  }
  var maxTap: Driver<Void> {
    return Driver.merge(base.coinMaxButton.rx.tap.asDriver(),
                        base.currencyMaxButton.rx.tap.asDriver())
  }
}

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
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
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
    coinAmountTextFieldController.placeholderText = "\(coinCode) \(localize(L.CoinWithdraw.Form.CoinAmount.placeholder))"
  }
}

extension Reactive where Base == ReserveFormView {
  var currencyText: ControlProperty<String?> {
    return base.currencyAmountTextField.rx.text
  }
  var coinText: ControlProperty<String?> {
    return base.coinAmountTextField.rx.text
  }
  var maxTap: Driver<Void> {
    return Driver.merge(base.coinMaxButton.rx.tap.asDriver(),
                        base.currencyMaxButton.rx.tap.asDriver())
  }
}

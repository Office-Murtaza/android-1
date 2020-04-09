import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import TrustWalletCore

final class CoinExchangeFormView: UIView, UIPickerViewDelegate, UIPickerViewDataSource, HasDisposeBag {
  
  let didSelectPickerRow = PublishRelay<CoinType>()
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let maxButton: MDCButton = {
    let button = MDCButton()
    button.setTitle(localize(L.CoinWithdraw.Button.max), for: .normal)
    button.setTitleColor(.ceruleanBlue, for: .normal)
    button.contentEdgeInsets = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8)
    button.setBackgroundColor(.white)
    return button
  }()
  
  let fromCoinAmountTextField: MDCTextField = {
    let textField = MDCTextField()
    textField.backgroundColor = .white
    textField.keyboardType = .decimalPad
    return textField
  }()
  
  let toCoinTextField: MDCTextField = {
    let textField = MDCTextField()
    textField.backgroundColor = .white
    return textField
  }()
  
  let toCoinPickerView: UIPickerView = {
    let picker = UIPickerView()
    picker.isHidden = true
    return picker
  }()
  
  let toCoinAmountTextField: MDCTextField = {
    let textField = MDCTextField()
    textField.backgroundColor = .white
    textField.keyboardType = .decimalPad
    textField.isEnabled = false
    return textField
  }()
  
  let toCoinTapRecognizer = UITapGestureRecognizer()
  
  let fromCoinAmountTextFieldController: MDCTextInputControllerOutlined
  let toCoinTextFieldController: MDCTextInputControllerOutlined
  let toCoinAmountTextFieldController: MDCTextInputControllerOutlined
  
  var coins: [CoinType] = [] {
    didSet {
      toCoinPickerView.reloadAllComponents()
    }
  }
  
  override init(frame: CGRect) {
    fromCoinAmountTextFieldController = MDCTextInputControllerOutlined(textInput: fromCoinAmountTextField)
    toCoinTextFieldController = MDCTextInputControllerOutlined(textInput: toCoinTextField)
    toCoinAmountTextFieldController = MDCTextInputControllerOutlined(textInput: toCoinAmountTextField)
    
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
    
    addSubview(stackView)
    stackView.addArrangedSubviews(fromCoinAmountTextField,
                                  toCoinTextField,
                                  toCoinPickerView,
                                  toCoinAmountTextField)

    fromCoinAmountTextField.trailingView = maxButton
    fromCoinAmountTextField.trailingViewMode = .always
    
    toCoinTextField.leadingViewMode = .always
    
    toCoinTextField.trailingView = UIImageView(image: UIImage(named: "dropdown"))
    toCoinTextField.trailingViewMode = .always
    
    let scheme = MDCContainerScheme()
    scheme.colorScheme = MDCSemanticColorScheme(defaults: .material201907)
    scheme.colorScheme.primaryColor = .ceruleanBlue
    scheme.colorScheme.onSurfaceColor = .warmGrey
    
    fromCoinAmountTextFieldController.applyTheme(withScheme: scheme)
    toCoinTextFieldController.applyTheme(withScheme: scheme)
    toCoinAmountTextFieldController.applyTheme(withScheme: scheme)
    
    toCoinTextField.addGestureRecognizer(toCoinTapRecognizer)
    
    toCoinPickerView.delegate = self
    toCoinPickerView.dataSource = self
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  private func setupBindings() {
    toCoinTapRecognizer.rx.event
      .asObservable()
      .map { [toCoinPickerView] _ in !toCoinPickerView.isHidden }
      .bind(to: toCoinPickerView.rx.isHidden)
      .disposed(by: disposeBag)
  }
  
  func configure(for coin: CoinType, and otherCoins: [CoinType]) {
    fromCoinAmountTextFieldController.placeholderText = String(format: localize(L.CoinExchange.Form.Amount.placeholder), coin.code)
    coins = otherCoins
  }
  
  func numberOfComponents(in pickerView: UIPickerView) -> Int {
    return 1
  }
  
  func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
    return coins.count
  }
  
  func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
    return coins[row].verboseValue
  }
  
  func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
    return didSelectPickerRow.accept(coins[row])
  }
}

extension Reactive where Base == CoinExchangeFormView {
  var fromCoinAmountText: ControlProperty<String?> {
    return base.fromCoinAmountTextField.rx.text
  }
  var toCoinTap: Driver<Void> {
    return base.toCoinTapRecognizer.rx.event.asDriver().map { _ in }
  }
  var toCoin: Binder<CoinType> {
    return Binder(base) { target, value in
      target.toCoinTextField.leadingView = UIImageView(image: value.smallLogo)
      target.toCoinTextFieldController.placeholderText = value.verboseValue
      target.toCoinAmountTextFieldController.placeholderText = String(format: localize(L.CoinExchange.Form.Amount.placeholder),
                                                                      value.code)
    }
  }
  var selectPickerItem: Driver<CoinType> {
    return base.didSelectPickerRow.asDriver(onErrorDriveWith: .empty())
  }
  var toCoinAmountText: ControlProperty<String?> {
    return base.toCoinAmountTextField.rx.text
  }
  var maxTap: Driver<Void> {
    return base.maxButton.rx.tap.asDriver()
  }
}

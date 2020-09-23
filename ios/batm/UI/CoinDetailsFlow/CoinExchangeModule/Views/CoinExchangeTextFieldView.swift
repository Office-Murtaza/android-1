import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import TrustWalletCore

final class CoinExchangeTextFieldView: UIView, UIPickerViewDelegate, UIPickerViewDataSource, HasDisposeBag {
  
  let didSelectPickerRow = PublishRelay<CustomCoinType>()
  
  let toCoinTextField = MDCTextField.dropdown
  let fakeToCoinTextField = FakeTextField()
  
  let toCoinPickerView = UIPickerView()
  
  let toCoinAmountLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .systemFont(ofSize: 16, weight: .medium)
    return label
  }()
  
  let toCoinTextFieldController: MDCTextInputControllerOutlined
  
  var coins: [CustomCoinType] = [] {
    didSet {
      toCoinPickerView.reloadAllComponents()
      toCoinTextField.isEnabled = coins.isNotEmpty
      fakeToCoinTextField.isEnabled = coins.isNotEmpty
    }
  }
  
  override init(frame: CGRect) {
    toCoinTextFieldController = ThemedTextInputControllerOutlined(textInput: toCoinTextField)
    
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(toCoinTextField,
                fakeToCoinTextField,
                toCoinAmountLabel)
  }
  
  private func setupLayout() {
    toCoinTextField.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    fakeToCoinTextField.snp.makeConstraints {
      $0.edges.equalTo(toCoinTextField)
    }
    toCoinAmountLabel.snp.makeConstraints {
      $0.top.equalTo(toCoinTextField.snp.bottom)
      $0.right.equalToSuperview().offset(-17)
      $0.left.greaterThanOrEqualToSuperview()
      $0.bottom.equalToSuperview()
    }
    
    setupPicker()
  }
  
  func setupPicker() {
    fakeToCoinTextField.inputView = toCoinPickerView
    
    toCoinPickerView.delegate = self
    toCoinPickerView.dataSource = self
  }
  
  func configure(for coins: [CustomCoinType]) {
    self.coins = coins
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

extension Reactive where Base == CoinExchangeTextFieldView {
  var toCoin: Binder<CustomCoinType> {
    return Binder(base) { target, value in
      target.toCoinTextField.setLeftView(UIImageView(image: value.smallLogo))
      target.toCoinTextFieldController.placeholderText = value.verboseValue
    }
  }
  var toCoinErrorText: Binder<String?> {
    return Binder(base) { target, value in
      target.toCoinTextFieldController.setErrorText(value, errorAccessibilityValue: value)
    }
  }
  var selectPickerItem: Driver<CustomCoinType> {
    return base.didSelectPickerRow.asDriver(onErrorDriveWith: .empty())
  }
  var toCoinAmountText: Binder<String?> {
    return base.toCoinAmountLabel.rx.text
  }
}

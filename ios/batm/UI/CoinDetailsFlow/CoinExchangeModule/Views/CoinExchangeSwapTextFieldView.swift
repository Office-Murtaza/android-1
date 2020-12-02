import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import TrustWalletCore

class CoinExchangeSwapTextFieldView: UIView, UIPickerViewDataSource, HasDisposeBag {
  
  let didSelectPickerRow = PublishRelay<CustomCoinType>()
  let willChangeCoinType = BehaviorRelay<CustomCoinType>(value: .bitcoin)
  let fakeToCoinTextField = FakeTextField()
  let coinTextField = UITextField()//MDCTextField.dropdown
//  let coinTextFieldController: MDCTextInputControllerOutlined
  let coinPickerView = UIPickerView()
  let maxButton = MDCButton.max
  
  lazy var coinTypeImageView: UIImageView = {
    let imageView = UIImageView()
    return imageView
  }()
  
  lazy var coinCheckMarkImageView: UIImageView = {
    let imageView = UIImageView()
    imageView.image = UIImage(named:"keyboard_arrow_down")
    return imageView
  }()
  
  lazy var balanceLabel: UILabel = {
    let label = UILabel()
    label.textColor = .slateGrey
    label.font = .systemFont(ofSize: 16, weight: .regular)
    //TODO: Remove
    label.text = "Balance"
    
    return label
  }();
  
  lazy var amountTextField: UITextField = {
    let textField = UITextField()
    textField.textAlignment = .right
    textField.text = "0"
    textField.font = .systemFont(ofSize: 22, weight: .bold)
    return textField
  }()
  
  
  var coins: [CustomCoinType] = [] {
      didSet {
          coinPickerView.reloadAllComponents()
          coinTextField.isEnabled = coins.isNotEmpty
          fakeToCoinTextField.isEnabled = coins.isNotEmpty
      }
  }
  
  override init(frame: CGRect) {
//      coinTextFieldController = ThemedTextInputControllerOutlined(textInput: coinTextField)
      
      super.init(frame: frame)
      
      setupUI()
      setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
      fatalError("init(coder:) has not been implemented")
  }
  
  
  private func setupUI() {
      translatesAutoresizingMaskIntoConstraints = false
      addSubviews(coinTextField,
                  fakeToCoinTextField,
                  coinTypeImageView,
                  coinCheckMarkImageView,
                  balanceLabel,
                  amountTextField,
                  maxButton)
    coinTextField.font = .systemFont(ofSize: 22, weight: .bold)
  }
  
  private func setupLayout() {
    
    coinTypeImageView.snp.makeConstraints {
      $0.top.equalToSuperview().offset(32)
      $0.left.equalToSuperview().offset(15)
      $0.width.equalTo(32)
      $0.height.equalTo(32)
    }
    
    balanceLabel.snp.makeConstraints {
      $0.top.equalTo(coinTypeImageView.snp.bottom).offset(10)
      $0.left.equalTo(coinTypeImageView)
    }
    
    coinTextField.snp.makeConstraints {
      $0.centerY.equalTo(coinTypeImageView.snp_centerYWithinMargins)
      $0.left.equalTo(coinTypeImageView.snp_rightMargin).offset(20)
      $0.height.equalTo(32)
      $0.width.greaterThanOrEqualTo(40)
    }
    
    coinCheckMarkImageView.snp.makeConstraints {
      $0.left.equalTo(coinTextField.snp.right).offset(5)
      $0.height.equalTo(coinTextField)
      $0.width.equalTo(30)
      $0.centerY.equalTo(coinTextField)
    }
    
    amountTextField.snp.makeConstraints {
      $0.right.equalToSuperview()
      $0.left.equalTo(coinCheckMarkImageView.snp.right)
      $0.centerY.equalTo(coinCheckMarkImageView)
    }
    
    fakeToCoinTextField.snp.makeConstraints {
      $0.edges.equalTo(coinTextField)
    }
    
    maxButton.snp.makeConstraints {
      $0.right.equalToSuperview().offset(5)
      $0.centerY.equalTo(balanceLabel)
    }
    
    setupPicker()
  }
  
  func setupPicker() {
      fakeToCoinTextField.inputView = coinPickerView
      
      coinPickerView.delegate = self
      coinPickerView.dataSource = self
  }
  
  
  func configure(for coins: [CustomCoinType]) {
      self.coins = coins
  }
  
}

extension CoinExchangeSwapTextFieldView: UIPickerViewDelegate {
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

extension Reactive where Base == CoinExchangeSwapTextFieldView {
    var toCoin: Binder<CustomCoinType> {
        return Binder(base) { target, value in
            base.willChangeCoinType.accept(CustomCoinType.allCases.first {
                $0.verboseValue == value.verboseValue
            } ?? .bitcoin)
//          target.coinTextField.setLeftView(UIImageView(image: value.mediumLogo))
          
          target.coinTypeImageView.image = value.mediumLogo
          target.coinTextField.text = value.code
//            target.coinTextFieldController.placeholderText = value.verboseValue
        }
    }
    var toCoinErrorText: Binder<String?> {
        return Binder(base) { target, value in
//            target.coinTextFieldController.setErrorText(value, errorAccessibilityValue: value)
        }
    }
    var selectPickerItem: Driver<CustomCoinType> {
        return base.didSelectPickerRow.asDriver(onErrorDriveWith: .empty())
    }
    var willCointTypeChanged: Driver<CustomCoinType> {
        return base.willChangeCoinType.asDriver(onErrorDriveWith: .empty())
    }
//    var toCoinAmountText: Binder<String?> {
//        return base.coinAmountLabel.rx.text
//    }
}


import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import TrustWalletCore

class CoinExchangeSwapTextFieldView: UIView, UIPickerViewDataSource, HasDisposeBag {
    
    let didSelectPickerRow = PublishRelay<CustomCoinType>()
    let willChangeCoinType = BehaviorRelay<CustomCoinType>(value: .bitcoin)
    let fakeToCoinTextField = FakeTextField()
    let coinTextField = UITextField()
    let coinPickerView = UIPickerView()
    let maxButton = MDCButton.max
    var coinType: CustomCoinType?
    
    lazy var errorField: UITextField = {
        let textField = UITextField()
        textField.leftViewMode = UITextField.ViewMode.always
        textField.isUserInteractionEnabled = false
        let containerView = UIView(frame: CGRect(x:0, y: 0,width:25, height: 20))
        let imageView = UIImageView(frame: CGRect(x: 0, y: 0, width:20 , height: 20))
        imageView.image = UIImage(named: "swap_error")
        containerView.addSubview(imageView)
        textField.leftView = containerView
        textField.textAlignment = .right
        textField.textColor = UIColor(hexString: "B00020")
        textField.font = .systemFont(ofSize: 12)
        return textField
    }()
    
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
        return label
    }();
    
    lazy var amountTextField: UITextField = {
        let textField = UITextField()
        textField.textAlignment = .right
        textField.attributedPlaceholder = NSAttributedString(string: "0", attributes: [.foregroundColor : UIColor.black])
        textField.font = .systemFont(ofSize: 22, weight: .bold)
        textField.keyboardType = .decimalPad
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
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
        
        errorField.text = "asfsadfs error"
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
                    maxButton,
                    errorField)
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
            $0.right.equalToSuperview().offset(-15)
            $0.left.equalTo(coinCheckMarkImageView.snp.right)
            $0.centerY.equalTo(coinCheckMarkImageView)
        }
        
        fakeToCoinTextField.snp.makeConstraints {
            $0.edges.equalTo(coinTextField)
        }
        
        maxButton.snp.makeConstraints {
            $0.right.equalToSuperview().offset(-8)
            $0.centerY.equalTo(balanceLabel)
        }
        
        errorField.snp.makeConstraints {
            $0.top.equalTo(maxButton.snp.bottom)
            $0.right.equalTo(amountTextField.snp.right)
            $0.left.greaterThanOrEqualTo(self)
        }
        
        setupPicker()
    }
    
    func setupPicker() {
        fakeToCoinTextField.inputView = coinPickerView
        
        coinPickerView.delegate = self
        coinPickerView.dataSource = self
    }
    
    
    
    func configure(for coinType:CustomCoinType,  coins: [CustomCoinType]) {
        self.coins = coins
        self.coinType = coinType
    }
    
    func configurBalance(for coinBalance: CoinBalance, useReserved: Bool = false, weighted: Bool = false) {
      let cryptoAmount = useReserved ? coinBalance.reservedBalance : coinBalance.balance
      let fiatAmount = useReserved ? coinBalance.reservedFiatBalance : coinBalance.fiatBalance
      
      configure(cryptoAmount: cryptoAmount, fiatAmount: fiatAmount, type: coinBalance.type, weighted: weighted)
    }
    
    private func configure(cryptoAmount: Decimal, fiatAmount: Decimal, type: CustomCoinType, weighted: Bool = false) {
        balanceLabel.text = "\(localize(L.CoinDetails.balance)): \(cryptoAmount.coinFormatted.withCoinType(type))"
        
        let font = UIFont.systemFont(ofSize: 16, weight: weighted ? .medium : .regular)
        balanceLabel.font = font
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
    var сoin: Binder<CustomCoinType> {
        return Binder(base) { target, value in
            base.willChangeCoinType.accept(CustomCoinType.allCases.first {
                $0.verboseValue == value.verboseValue
            } ?? .bitcoin)
            target.coinTypeImageView.image = value.mediumLogo
            target.coinTextField.text = value.code
        }
    }
    
    var selectPickerItem: Driver<CustomCoinType> {
        return base.didSelectPickerRow.asDriver(onErrorDriveWith: .empty())
    }
    
    var willCointTypeChanged: Driver<CustomCoinType> {
        return base.willChangeCoinType.asDriver(onErrorDriveWith: .empty())
    }
    
    var coinAmountText: ControlProperty<String?> {
        return base.amountTextField.rx.text
    }
    
    var maxTap: Driver<Void> {
        return base.maxButton.rx.tap.asDriver()
    }
}


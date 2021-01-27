import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import TrustWalletCore

class CoinExchangeFormView: UIView {
    lazy var swapButton: SwapButton = {
        let button = SwapButton(type: .system)
        return button
    }()
    
    let swapRateView = СoinRateView()
    
    lazy var fromCoinView: CoinExchangeSwapTextFieldView = {
        let textFieldView = CoinExchangeSwapTextFieldView()
        return textFieldView
    }()
    
    lazy var toCoinView: CoinExchangeSwapTextFieldView = {
        let textFieldView = CoinExchangeSwapTextFieldView()
        textFieldView.backgroundColor = .textfieldLightGray
        return textFieldView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(coin: CustomCoinType, fromCoins: [CustomCoinType], toCoins:  [CustomCoinType], fee: Decimal?) {
        fromCoinView.configure(for: coin, coins: fromCoins)
        toCoinView.configure(for: coin, coins: toCoins)
    }
    
    func configureRateView(fromCoin: String, toCoin: String) {
        swapRateView.tildaLabelText = localize(L.Swap.tilda)
        swapRateView.trandingViewImage = UIImage(named: "trending_up")
        swapRateView.configure(fromCoin: fromCoin, toCoin: toCoin)
    }

    func configureFromError(error: String?) {
        configureField(field: &fromCoinView, error: error)
    }
    
    func configureToError(error: String?) {
        configureField(field: &toCoinView, error: error)
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        
        addSubviews([
            fromCoinView,
            toCoinView,
            swapButton,
            swapRateView,
        ])
    }
    
    private func setupLayout() {
        fromCoinView.snp.remakeConstraints {
            $0.top.left.right.equalToSuperview()
            $0.height.equalTo(136)
        }
        toCoinView.snp.remakeConstraints {
            $0.top.equalTo(fromCoinView.snp.bottom)
            $0.left.right.equalToSuperview()
            $0.height.equalTo(136)
        }
        
        swapButton.snp.makeConstraints {
            $0.width.height.equalTo(36)
            $0.centerY.equalTo(fromCoinView.snp.bottom)
            $0.left.equalToSuperview().offset(15)
        }
        
        swapRateView.snp.makeConstraints {
            $0.height.equalTo(36)
            $0.centerY.equalTo(fromCoinView.snp.bottom)
            $0.right.equalToSuperview().offset(-15)
        }
    }
    
    private func configureField(field: inout CoinExchangeSwapTextFieldView, error: String?) {
        field.errorFieldView.isHidden = error == nil
        field.setupErrorField(errorText: error)
        field.amountTextField.textColor = error == nil ? .black : .errorRed
        
        fromCoinView.snp.remakeConstraints {
            $0.top.left.right.equalToSuperview()
            $0.height.equalTo(error == nil ? 136 : 150)
        }
        toCoinView.snp.remakeConstraints {
            $0.top.equalTo(fromCoinView.snp.bottom)
            $0.left.right.equalToSuperview()
            $0.height.equalTo(error == nil ? 136 : 150)
        }
    }
}

extension Reactive where Base == CoinExchangeFormView {
    //MARK: - From coin
    
    var fromCoin: Binder<CustomCoinType> {
        return base.fromCoinView.rx.сoin
    }
    
    var fromCoinAmountText: ControlProperty<String?> {
        return base.fromCoinView.rx.coinAmountText
    }
    
    var willChangeFromCoinType: Driver<CustomCoinType> {
        return base.fromCoinView.rx.willCointTypeChanged
    }
    
    var selectFromPickerItem: Driver<CustomCoinType> {
        return base.fromCoinView.rx.selectPickerItem
    }
    
    var maxFromTap: Driver<Void> {
        return base.fromCoinView.rx.maxTap
    }
    
    //MARK: - To coin
    
    var toCoin: Binder<CustomCoinType> {
        return base.toCoinView.rx.сoin
    }

    var selectToPickerItem: Driver<CustomCoinType> {
        return base.toCoinView.rx.selectPickerItem
    }
    var toCoinAmountText: ControlProperty<String?> {
        return base.toCoinView.rx.coinAmountText
    }
    var maxToTap: Driver<Void> {
        return base.toCoinView.rx.maxTap
    }
    var willChangeToCoinType: Driver<CustomCoinType> {
        return base.toCoinView.rx.willCointTypeChanged
    }
    
    var swapButtonDidPushed: ControlEvent<Void> {
        return base.swapButton.rx.tap
    }
}

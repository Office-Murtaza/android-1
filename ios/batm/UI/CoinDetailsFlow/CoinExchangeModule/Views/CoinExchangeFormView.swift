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
    
    let swapRateView = SwapExchangeRateView()
    
    lazy var fromCoinView: CoinExchangeSwapTextFieldView = {
        let textFieldView = CoinExchangeSwapTextFieldView()
        return textFieldView
    }()
    
    lazy var toCoinView: CoinExchangeSwapTextFieldView = {
        let textFieldView = CoinExchangeSwapTextFieldView()
        textFieldView.backgroundColor = UIColor(red: 0.553, green: 0.553, blue: 0.553, alpha: 0.1)
        return textFieldView
    }()
    
    private let swapFeeView = SwapPlatformFeeView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        
        addSubviews([
            fromCoinView,
            toCoinView,
            swapButton,
            swapRateView,
            swapFeeView
        ])
    }
    
  private func setupLayout() {
    fromCoinView.snp.makeConstraints {
        $0.top.left.right.equalToSuperview()
        $0.height.equalTo(150)
    }
    toCoinView.snp.makeConstraints {
        $0.top.equalTo(fromCoinView.snp.bottom)
        $0.left.right.equalToSuperview()
        $0.height.equalTo(150)
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
    
    swapFeeView.snp.makeConstraints {
        $0.height.equalTo(60)
        $0.left.right.equalToSuperview()
        $0.top.equalTo(toCoinView.snp.bottom)
    }
    
  }
    
    func configure(coin: CustomCoinType, fromCoins: [CustomCoinType], toCoins:  [CustomCoinType], fee: Decimal?) {
        fromCoinView.configure(for: coin, coins: fromCoins)
        toCoinView.configure(for: coin, coins: toCoins)
    }
    
    func configureRateView(fromCoin: String, toCoin: String) {
        swapRateView.configure(fromCoin: fromCoin, toCoin: toCoin)
    }
    
    func configureFeeView(fee: String) {
        swapFeeView.configure(fee: fee)
    }
    
    func configureFromError(error: String?) {
        configureField(field: &fromCoinView, error: error)
    }
    
    func configureToError(error: String?) {
        configureField(field: &toCoinView, error: error)
    }
    
    private func configureField(field: inout CoinExchangeSwapTextFieldView, error: String?) {
        field.errorField.isHidden = error == nil
        field.errorField.text = error
        field.amountTextField.textColor = error == nil ? UIColor.black : UIColor(hexString: "B00020")
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
    
  //property from the CoinAmountTextFieldView
//    var fromCoinFiatAmountText: Binder<String?> {
//        return base.fromCoinAmountTextFieldView.rx.fiatAmountText
//    }
    
    //TODO: fromMaxTap???
    
    //MARK: - To coin
    
    var toCoin: Binder<CustomCoinType> {
        return base.toCoinView.rx.сoin
    }
    var toCoinErrorText: Binder<String?> {
        return base.toCoinView.rx.сoinErrorText
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

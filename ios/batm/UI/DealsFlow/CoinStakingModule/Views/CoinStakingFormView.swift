import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import TrustWalletCore

class CoinStakingFormView: UIView {
    let stakingRateView = СoinRateView()
    fileprivate lazy var fromCoinView: CoinExchangeSwapTextFieldView = {
        let textFieldView = CoinExchangeSwapTextFieldView()
        textFieldView.backgroundColor = .textfieldLightGray
        textFieldView.coinCheckMarkImageView.image = nil
        return textFieldView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
        setupLayout()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configureStakeAmount(with amount: String) {
        fromCoinView.amountTextField.text = amount
    }
    
    func configure(coinType: CustomCoinType, stakeDetails: StakeDetails, fee: Decimal?) {
        fromCoinView.configure(for: coinType, coins: [coinType])
        switch stakeDetails.status {
        case .notExist, .withdrawn:
            break
        default:
            fromCoinView.maxButton.isHidden = true
            fromCoinView.resultLabel.text = localize(L.CoinStaking.Status.formViewStatus)
            fromCoinView.amountTextField.isEnabled = false
        }
      }
    
    func configureRateView(fromCoin: String, toCurrency: String) {
        stakingRateView.tildaLabelText = " = "
        stakingRateView.trandingViewImage = UIImage(named: "local_offer")
        stakingRateView.configure(fromCoin: fromCoin, toCoin: toCurrency)
    }
    
    func configure(from error: String?) {
        configureField(field: &fromCoinView, error: error)
    }
    
    func configureBalance(for coinBalance: CoinBalance, coinDetails: CoinDetails) {
        fromCoinView.configureBalance(for: coinBalance, coinDetails: coinDetails)
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        addSubviews([fromCoinView, stakingRateView])
    }
    
    private func setupLayout() {
        fromCoinView.snp.makeConstraints {
            $0.top.left.right.equalToSuperview()
            $0.height.equalTo(150)
        }
        
        stakingRateView.snp.makeConstraints {
            $0.height.equalTo(36)
            $0.centerY.equalTo(fromCoinView.snp.bottom)
            $0.left.equalToSuperview().offset(15)
        }
    }
    
    private func configureField(field: inout CoinExchangeSwapTextFieldView, error: String?) {
        field.errorFieldView.isHidden = error == nil
        field.setupErrorField(errorText: error)
        field.amountTextField.textColor = error == nil ? .black : .errorRed
    }
}

extension Reactive where Base == CoinStakingFormView {
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
}

import UIKit
import RxSwift
import RxCocoa
import MaterialComponents
import TrustWalletCore

final class CoinExchangeFormView: UIView, HasDisposeBag {
    
    let stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        return stackView
    }()
    
    let fromCoinAmountTextFieldView = CoinAmountTextFieldView()
    let toCoinTextFieldView = CoinExchangeTextFieldView()
    
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
        
        addSubview(stackView)
        stackView.addArrangedSubviews(fromCoinAmountTextFieldView,
                                      toCoinTextFieldView)
    }
    
    private func setupLayout() {
        stackView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
    
    func configure(coin: CustomCoinType, otherCoins: [CustomCoinType], fee: Decimal?) {
        fromCoinAmountTextFieldView.configure(coinType: coin, fee: fee)
        toCoinTextFieldView.configure(for: otherCoins)
    }
}

extension Reactive where Base == CoinExchangeFormView {
    var fromCoinAmountText: ControlProperty<String?> {
        return base.fromCoinAmountTextFieldView.rx.coinAmountText
    }
    var fromCoinAmountErrorText: Binder<String?> {
        return base.fromCoinAmountTextFieldView.rx.coinAmountErrorText
    }
    var fromCoinFiatAmountText: Binder<String?> {
        return base.fromCoinAmountTextFieldView.rx.fiatAmountText
    }
    var toCoin: Binder<CustomCoinType> {
        return base.toCoinTextFieldView.rx.toCoin
    }
    var toCoinErrorText: Binder<String?> {
        return base.toCoinTextFieldView.rx.toCoinErrorText
    }
    var selectPickerItem: Driver<CustomCoinType> {
        return base.toCoinTextFieldView.rx.selectPickerItem
    }
    var toCoinAmountText: Binder<String?> {
        return base.toCoinTextFieldView.rx.toCoinAmountText
    }
    var maxTap: Driver<Void> {
        return base.fromCoinAmountTextFieldView.rx.maxTap
    }
    var willChangeCoinType: Driver<CustomCoinType> {
        return base.toCoinTextFieldView.rx.willCointTypeChanged
    }
}

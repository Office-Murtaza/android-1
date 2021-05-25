import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class ReserveFormView: UIView, HasDisposeBag {
    let coinMaxButton = MDCButton.max
    
    let coinAmountTextFieldView = CoinAmountTextFieldView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(coinType: CustomCoinType, fee: Decimal) {
        coinAmountTextFieldView.configure(coinType: coinType, fee: fee)
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        
        setupTextFields()
        addSubviews(coinAmountTextFieldView)
    }
    
    private func setupLayout() {
        coinAmountTextFieldView.snp.makeConstraints {
            $0.edges.equalToSuperview()
        }
    }
    
    private func setupTextFields() {
        let toolbar = UIToolbar()
        let flexSpace = UIBarButtonItem(barButtonSystemItem: .flexibleSpace,
                                        target: nil,
                                        action: nil)
        let doneButton = UIBarButtonItem(title: localize(L.Shared.Button.done),
                                         style: .done,
                                         target: self,
                                         action: #selector(doneButtonTapped))
        
        toolbar.setItems([flexSpace, doneButton], animated: true)
        toolbar.sizeToFit()
        
        coinAmountTextFieldView.coinAmountTextField.inputAccessoryView = toolbar
    }
    
    @objc private func doneButtonTapped() {
        endEditing(true)
    }
}

extension Reactive where Base == ReserveFormView {
    var fiatAmountText: Binder<String?> {
        return base.coinAmountTextFieldView.rx.fiatAmountText
    }
    var coinAmountText: ControlProperty<String?> {
        return base.coinAmountTextFieldView.rx.coinAmountText
    }
    var coinAmountErrorText: Binder<String?> {
        return base.coinAmountTextFieldView.rx.coinAmountErrorText
    }
    var maxTap: Driver<Void> {
        return base.coinAmountTextFieldView.rx.maxTap
    }
}

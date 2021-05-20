import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class CoinWithdrawFormView: UIView, HasDisposeBag {
    
    let addressButtonsStackView = UIStackView()
    
    let pasteButton = MDCButton.paste
    let scanButton = MDCButton.scan
    
    let addressTextField = MDCOutlinedTextArea.address
    let coinAmountTextFieldView = CoinAmountTextFieldView()
    
    override init(frame: CGRect) {
        
        super.init(frame: frame)
        
        setupUI()
        setupLayout()
        setupBindings()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(coinType: CustomCoinType, fee: Decimal?) {
        coinAmountTextFieldView.configure(coinType: coinType, fee: fee)
    }
    
    private func setupUI() {
        translatesAutoresizingMaskIntoConstraints = false
        setupTextFields()

        addSubviews(addressTextField,
                    coinAmountTextFieldView)
        
        addressButtonsStackView.addArrangedSubviews(pasteButton,
                                                    scanButton)
        
        addressTextField.setRightView(addressButtonsStackView)
        addressTextField.label.text = localize(L.CoinWithdraw.Form.RecipientAddress.placeholder)
    }
    
    private func setupLayout() {
        addressTextField.snp.makeConstraints {
            $0.top.left.right.equalToSuperview()
        }
        coinAmountTextFieldView.snp.makeConstraints {
            $0.top.equalTo(addressTextField.snp.bottom).offset(20)
            $0.left.right.bottom.equalToSuperview()
        }
    }
    
    private func setupBindings() {
        addressTextField.rx.text
            .asDriver()
            .filterNil()
            .filterEmpty()
            .distinctUntilChanged()
            .drive(onNext: { [addressTextField] _ in
                if !addressTextField.textView.isFirstResponder {
                    addressTextField.textView.becomeFirstResponder()
                }
            })
            .disposed(by: disposeBag)
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
        
        addressTextField.textView.inputAccessoryView = toolbar
        coinAmountTextFieldView.coinAmountTextField.inputAccessoryView = toolbar
    }
    
    @objc private func doneButtonTapped() {
        endEditing(true)
    }
}

extension Reactive where Base == CoinWithdrawFormView {
    var fiatAmountText: Binder<String?> {
        return base.coinAmountTextFieldView.rx.fiatAmountText
    }
    var coinAmountText: ControlProperty<String?> {
        return base.coinAmountTextFieldView.rx.coinAmountText
    }
    var addressText: ControlProperty<String?> {
        base.addressTextField.textView.sizeToFit()
        return base.addressTextField.rx.text
    }
    var coinAmountErrorText: Binder<String?> {
        return base.coinAmountTextFieldView.rx.coinAmountErrorText
    }
    var addressErrorText: Binder<String?> {
        return Binder(base) { target, value in
            target.addressTextField.setErrorText(value)
        }
    }
    var maxTap: Driver<Void> {
        return base.coinAmountTextFieldView.rx.maxTap
    }
    var pasteTap: Driver<Void> {
        return base.pasteButton.rx.tap.asDriver()
    }
    var scanTap: Driver<Void> {
        return base.scanButton.rx.tap.asDriver()
    }
}

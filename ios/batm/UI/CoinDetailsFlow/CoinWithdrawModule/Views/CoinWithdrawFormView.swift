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
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
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
  
  func configure(with coinCode: String) {
    coinAmountTextFieldView.configure(with: coinCode)
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

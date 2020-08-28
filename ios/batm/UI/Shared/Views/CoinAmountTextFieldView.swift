import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class CoinAmountTextFieldView: UIView, HasDisposeBag {
  
  let maxButton = MDCButton.max
  
  let coinAmountTextField = MDCTextField.amount
  
  let coinAmountTextFieldController: ThemedTextInputControllerOutlined
  
  let fiatAmountLabel: UILabel = {
    let label = UILabel()
    label.textColor = .ceruleanBlue
    label.font = .systemFont(ofSize: 16, weight: .medium)
    return label
  }()
  
  override init(frame: CGRect) {
    coinAmountTextFieldController = ThemedTextInputControllerOutlined(textInput: coinAmountTextField)
    
    super.init(frame: frame)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(coinAmountTextField,
                fiatAmountLabel)

    coinAmountTextField.setRightView(maxButton)
    
    coinAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CoinAmount.placeholder)
  }
  
  private func setupLayout() {
    coinAmountTextField.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
    }
    fiatAmountLabel.snp.makeConstraints {
      $0.top.equalTo(coinAmountTextField.snp.bottom).offset(-10)
      $0.right.equalToSuperview().offset(-17)
      $0.left.greaterThanOrEqualToSuperview()
      $0.bottom.equalToSuperview()
    }
  }
  
  func configure(with coinCode: String) {
    coinAmountTextFieldController.placeholderText = "\(coinCode) \(localize(L.CoinWithdraw.Form.CoinAmount.placeholder))"
  }
}

extension Reactive where Base == CoinAmountTextFieldView {
  var coinAmountText: ControlProperty<String?> {
    return base.coinAmountTextField.rx.text
  }
  var fiatAmountText: Binder<String?> {
     return base.fiatAmountLabel.rx.text
   }
  var maxTap: Driver<Void> {
    return base.maxButton.rx.tap.asDriver()
  }
}

import UIKit
import RxSwift
import RxCocoa
import MaterialComponents

final class CoinStakingFormView: UIView {
  
  let stackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    return stackView
  }()
  
  let coinMaxButton = MDCButton.max
  let coinAmountTextField = MDCTextField.amount
  let coinAmountTextFieldController: MDCTextInputControllerOutlined
  
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
    
    addSubviews(stackView)
    stackView.addArrangedSubviews(coinAmountTextField)
    
    coinAmountTextField.setRightView(coinMaxButton)
    
    coinAmountTextFieldController.placeholderText = localize(L.CoinWithdraw.Form.CoinAmount.placeholder)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  func configure(with coinCode: String, stakeDetails: StakeDetails) {
    coinAmountTextFieldController.placeholderText = "\(coinCode) \(localize(L.CoinWithdraw.Form.CoinAmount.placeholder))"
    
    coinAmountTextField.isHidden = stakeDetails.exist
  }
}

extension Reactive where Base == CoinStakingFormView {
  var coinText: ControlProperty<String?> {
    return base.coinAmountTextField.rx.text
  }
  var maxTap: Driver<Void> {
    return base.coinMaxButton.rx.tap.asDriver()
  }
}

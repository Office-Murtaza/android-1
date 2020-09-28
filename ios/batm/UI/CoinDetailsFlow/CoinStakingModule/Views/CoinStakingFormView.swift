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
  
  let coinAmountTextFieldView = CoinAmountTextFieldView()
  
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
    stackView.addArrangedSubview(coinAmountTextFieldView)
  }
  
  private func setupLayout() {
    stackView.snp.makeConstraints {
      $0.edges.equalToSuperview()
    }
  }
  
  func configure(coinType: CustomCoinType, stakeDetails: StakeDetails, fee: Decimal?) {
    coinAmountTextFieldView.configure(coinType: coinType, fee: fee)
    coinAmountTextFieldView.isHidden = stakeDetails.status != .notCreatedOrWithdrawn
  }
}

extension Reactive where Base == CoinStakingFormView {
  var coinAmountText: ControlProperty<String?> {
    return base.coinAmountTextFieldView.rx.coinAmountText
  }
  var coinAmountErrorText: Binder<String?> {
    return base.coinAmountTextFieldView.rx.coinAmountErrorText
  }
  var fiatAmountText: Binder<String?> {
     return base.coinAmountTextFieldView.rx.fiatAmountText
  }
  var maxTap: Driver<Void> {
    return base.coinAmountTextFieldView.rx.maxTap
  }
}

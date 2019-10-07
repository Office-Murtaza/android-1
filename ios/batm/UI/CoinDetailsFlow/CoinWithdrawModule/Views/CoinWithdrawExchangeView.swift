import UIKit
import RxSwift
import RxCocoa

class CoinWithdrawExchangeView: UIView {
  
  let amountLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinWithdraw.Form.Amount.title)
    label.textColor = .slateGrey
    label.font = .poppinsSemibold14
    return label
  }()
  
  let currencyContainer = UIView()
  let coinContainer = UIView()
  let exchangeImageView = UIImageView(image: UIImage(named: "exchange"))
  
  let currencyLabel: UILabel = {
    let label = UILabel()
    label.text = "USD"
    label.textColor = .warmGrey
    label.font = .poppinsMedium14
    return label
  }()
  
  let coinLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .poppinsMedium14
    return label
  }()
  
  let currencyTextField: MainTextField = {
    let textField = MainTextField()
    textField.textAlignment = .center
    textField.keyboardType = .decimalPad
    return textField
  }()
  
  let coinTextField: MainTextField = {
    let textField = MainTextField()
    textField.textAlignment = .center
    textField.keyboardType = .decimalPad
    return textField
  }()
  
  let maxLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .max)
    return label
  }()
  
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
    
    addSubviews(amountLabel,
                currencyContainer,
                exchangeImageView,
                coinContainer,
                maxLabel)
    currencyContainer.addSubviews(currencyLabel,
                                  currencyTextField)
    coinContainer.addSubviews(coinLabel,
                              coinTextField)
  }
  
  private func setupLayout() {
    amountLabel.snp.makeConstraints {
      $0.top.equalToSuperview()
      $0.centerX.equalToSuperview()
    }
    currencyContainer.snp.makeConstraints {
      $0.top.equalTo(amountLabel.snp.bottom).offset(15)
      $0.left.equalToSuperview()
    }
    exchangeImageView.snp.makeConstraints {
      $0.centerY.equalTo(currencyTextField.snp.centerY)
      $0.left.equalTo(currencyContainer.snp.right).offset(18)
    }
    exchangeImageView.setContentHuggingPriority(.required, for: .horizontal)
    exchangeImageView.setContentCompressionResistancePriority(.required, for: .horizontal)
    coinContainer.snp.makeConstraints {
      $0.top.equalTo(amountLabel.snp.bottom).offset(15)
      $0.right.equalToSuperview()
      $0.left.equalTo(exchangeImageView.snp.right).offset(18)
      $0.width.equalTo(currencyContainer)
    }
    [currencyLabel, coinLabel].forEach {
      $0.snp.makeConstraints {
        $0.top.centerX.equalToSuperview()
      }
    }
    [currencyTextField, coinTextField].forEach {
      $0.snp.makeConstraints {
        $0.top.equalTo(currencyLabel.snp.bottom).offset(16)
        $0.left.right.bottom.equalToSuperview()
      }
    }
    maxLabel.snp.makeConstraints {
      $0.top.equalTo(currencyContainer.snp.bottom).offset(15)
      $0.centerX.bottom.equalToSuperview()
    }
  }
  
  func configure(with coinCode: String) {
    coinLabel.text = coinCode
  }
}

extension Reactive where Base == CoinWithdrawExchangeView {
  var currencyText: Driver<String?> {
    return base.currencyTextField.rx.text.asDriver()
  }
  var coinText: Driver<String?> {
    return base.coinTextField.rx.text.asDriver()
  }
  var maxTap: Driver<Void> {
    return base.maxLabel.rx.tap
  }
}


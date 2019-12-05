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
  
  let coinStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 15
    return stackView
  }()
  
  let currencyStackView: UIStackView = {
    let stackView = UIStackView()
    stackView.axis = .vertical
    stackView.spacing = 15
    return stackView
  }()
  
  let exchangeImageView = UIImageView(image: UIImage(named: "exchange"))
  
  let coinLabel: UILabel = {
    let label = UILabel()
    label.textColor = .warmGrey
    label.font = .poppinsMedium14
    label.textAlignment = .center
    return label
  }()
  
  let currencyLabel: UILabel = {
    let label = UILabel()
    label.text = "USD"
    label.textColor = .warmGrey
    label.font = .poppinsMedium14
    label.textAlignment = .center
    return label
  }()
  
  let coinTextField: MainTextField = {
    let textField = MainTextField()
    textField.textAlignment = .center
    textField.keyboardType = .decimalPad
    return textField
  }()
  
  let currencyTextField: MainTextField = {
    let textField = MainTextField()
    textField.textAlignment = .center
    textField.keyboardType = .decimalPad
    return textField
  }()
  
  let currencySellLabel: UILabel = {
    let label = UILabel()
    label.text = localize(L.CoinSell.annotation)
    label.textColor = .slateGrey
    label.textAlignment = .center
    label.font = .poppinsMedium10
    label.minimumScaleFactor = 0.5
    label.adjustsFontSizeToFitWidth = true
    label.numberOfLines = 2
    return label
  }()
  
  let maxLabel: UnderlinedLabelView = {
    let label = UnderlinedLabelView()
    label.configure(for: .max)
    return label
  }()
  
  private let isSellMode: Bool
  
  init(sellMode: Bool = false) {
    self.isSellMode = sellMode
    
    super.init(frame: .null)
    
    setupUI()
    setupLayout()
  }
  
  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  private func setupUI() {
    translatesAutoresizingMaskIntoConstraints = false
    
    addSubviews(amountLabel,
                coinStackView,
                exchangeImageView,
                currencyStackView,
                maxLabel)
    coinStackView.addArrangedSubviews(coinLabel,
                                      coinTextField)
    currencyStackView.addArrangedSubviews(currencyLabel,
                                          currencyTextField,
                                          currencySellLabel)
    
    coinTextField.isEnabled = !isSellMode
    currencySellLabel.isHidden = !isSellMode
  }
  
  private func setupLayout() {
    amountLabel.snp.makeConstraints {
      $0.top.equalToSuperview()
      $0.centerX.equalToSuperview()
    }
    coinStackView.snp.makeConstraints {
      $0.top.equalTo(amountLabel.snp.bottom).offset(15)
      $0.left.equalToSuperview()
    }
    exchangeImageView.snp.makeConstraints {
      $0.centerY.equalTo(coinTextField.snp.centerY)
      $0.left.equalTo(coinStackView.snp.right).offset(18)
    }
    exchangeImageView.setContentHuggingPriority(.required, for: .horizontal)
    exchangeImageView.setContentCompressionResistancePriority(.required, for: .horizontal)
    currencyStackView.snp.makeConstraints {
      $0.top.equalTo(amountLabel.snp.bottom).offset(15)
      $0.right.equalToSuperview()
      $0.left.equalTo(exchangeImageView.snp.right).offset(18)
      $0.width.equalTo(coinStackView)
    }
    maxLabel.snp.makeConstraints {
      $0.top.equalTo(currencyStackView.snp.bottom).offset(15)
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


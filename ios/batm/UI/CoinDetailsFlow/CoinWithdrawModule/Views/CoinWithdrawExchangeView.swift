import UIKit

class CoinWithdrawExchangeView: UIView {
  
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
    
    addSubviews(currencyContainer,
                exchangeImageView,
                coinContainer)
    currencyContainer.addSubviews(currencyLabel,
                                  currencyTextField)
    coinContainer.addSubviews(coinLabel,
                              coinTextField)
  }
  
  private func setupLayout() {
    currencyContainer.snp.makeConstraints {
      $0.top.left.bottom.equalToSuperview()
    }
    exchangeImageView.snp.makeConstraints {
      $0.centerY.equalTo(currencyTextField.snp.centerY)
      $0.left.equalTo(currencyContainer.snp.right).offset(18)
    }
    exchangeImageView.setContentHuggingPriority(.required, for: .horizontal)
    exchangeImageView.setContentCompressionResistancePriority(.required, for: .horizontal)
    coinContainer.snp.makeConstraints {
      $0.top.right.bottom.equalToSuperview()
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
  }
  
  func configure(with coinCode: String) {
    coinLabel.text = coinCode
  }
}


import UIKit
import SnapKit
import MaterialComponents

protocol P2PCreateOrderPopupViewControllerDelegate: AnyObject {
    func didTapCreateOrder(model: P2PCreateOrderDataModel)
}

class P2PCreateOrderPopupViewController: UIViewController {
  
  private let containerView = UIView()
  private let orderAmountView = P2PCreateOrderAmountView()
  private var tradePrice: Double = 0
    private var platformFee: Double = 0
  private let presenter = P2PCreateOrderPresenter()
    private var coinCode = ""
    
  private let platformFeeTitleLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    return label
  }()
  
  private let platformFeeValueLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    return label
  }()
  
  private let platformFeeStackView: UIStackView = {
    let stack = UIStackView()
    stack.distribution = .equalCentering
    stack.axis = .horizontal
    return stack
  }()
  
  private let submitButton = MDCButton.submit
  
  override func viewDidLoad() {
    super.viewDidLoad()
    presenter.output = self
    view.backgroundColor = UIColor.black.withAlphaComponent(0.3)
    orderAmountView.fiatTextField.becomeFirstResponder()
    orderAmountView.fiatTextField.addTarget(self, action: #selector(textChanged(_:)), for: .editingChanged)
    setupUI()
    setupLayout()
    setupRecognizers()
  }
    
    @objc private func textChanged(_ textField: UITextField) {
       guard let fiatAmount = textField.text else { return }
       presenter.updatedFiatAmount(fiat: fiatAmount, fee: platformFee, price: tradePrice)
    }
    
  
  func setupRecognizers() {
    let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(hideController))
    let swipeRecogizer = UISwipeGestureRecognizer(target: self, action: #selector(hideController))
    swipeRecogizer.direction = .down
    
    view.addGestureRecognizer(tapRecognizer)
    view.addGestureRecognizer(swipeRecogizer)
  }
  
  @objc func hideController() {
    dismiss(animated: true, completion: nil)
  }
  
  func setupUI() {
    containerView.backgroundColor = .white
    view.addSubviews([
      containerView
    ])
    
    containerView.addSubviews([
      orderAmountView,
      platformFeeStackView,
      submitButton
    ])
    
    platformFeeStackView.addArrangedSubviews([
      platformFeeTitleLabel,
      platformFeeValueLabel
    ])
    
    platformFeeTitleLabel.text = localize(L.P2p.Create.Order.platformFee)
    platformFeeValueLabel.text = "0"
    
    submitButton.addTarget(self, action: #selector(createOrder), for: .touchUpInside)
    
  }
    
    func setup(tradePrice: Double, platformFee: Double, coinCode: String) {
        self.tradePrice = tradePrice
        self.platformFee = platformFee
        self.coinCode = coinCode
        orderAmountView.cryptoAmountValue.text = "0 \(coinCode)"
    }

    @objc private func createOrder() {
//        let model = P2PCreateOrderModel(tradeId: <#T##String#>, price: <#T##Double#>, cryptoAmount: <#T##Double#>, fiatAmount: <#T##Double#>)
    }

  func setupLayout() {
    orderAmountView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(100)
    }
    
    containerView.snp.makeConstraints {
      $0.left.right.equalToSuperview()
      $0.bottom.equalToSuperview().offset(-300)
      $0.height.equalTo(236)
    }
    
    platformFeeStackView.snp.makeConstraints {
      $0.top.equalTo(orderAmountView.snp.bottom).offset(15)
      $0.centerX.equalToSuperview()
    }
    
    submitButton.snp.makeConstraints {
      $0.top.equalTo(platformFeeStackView.snp.bottom).offset(25)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
      $0.height.equalTo(50)
    }
    
  }
  
}

extension P2PCreateOrderPopupViewController: P2PCreateOrderPresenterOutput {
    func updated(crypto: String, fee: String) {
        orderAmountView.cryptoAmountValue.text = "\(crypto) \(coinCode)"
        platformFeeValueLabel.text = fee
    }
}

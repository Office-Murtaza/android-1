import UIKit
import SnapKit
import MaterialComponents

protocol P2PCreateOrderPopupViewControllerDelegate: AnyObject {
    func createOrder(price: Double, cryptoAmount: Double, fiatAmount: Double)
}

class P2PCreateOrderPopupViewController: UIViewController {
    
    private let containerView = UIView()
    private let fiatAmountView = P2PCreateOrderFiatAmountView()
    private var tradePrice: Double = 0
    private var platformFee: Double = 0
    private let presenter = P2PCreateOrderPresenter()
    private var coinCode = ""
    private let errorView = P2PInlineErrorView()
    private var cryptoAmount: Double = 0
    private var fiatAmount: Double = 0
    private var trade: Trade?
    private var currentError: P2PCreateOrderValidationError?
  
    private var reserverdInfoLine = P2PCreateOrderInfoLine()
    private var cryptoAmountInfoLine = P2PCreateOrderInfoLine()
    private var platformFeeInfoLine = P2PCreateOrderInfoLine()
    private var currentType: TradeType = .unknown
  
  private lazy var youWillLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 12)
    label.textColor = UIColor.black.withAlphaComponent(0.6)
    return label
  }()
  
  private lazy var resultLabel: UILabel = {
    let label = UILabel()
    label.font = .systemFont(ofSize: 22)
    return label
  }()
  
    weak var delegate: P2PCreateOrderPopupViewControllerDelegate?
    private let submitButton = MDCButton.submit
    private var reservedBalance: Double = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        presenter.output = self
        view.backgroundColor = UIColor.black.withAlphaComponent(0.3)
        fiatAmountView.fiatTextField.becomeFirstResponder()
        fiatAmountView.fiatTextField.addTarget(self, action: #selector(textChanged(_:)), for: .editingChanged)
        setupUI()
        setupLayout()
        setupRecognizers()
    }
    
    @objc private func textChanged(_ textField: UITextField) {
        guard let fiatAmount = textField.text, let trade = trade else {
          return
        }
      
        resetError()
        presenter.updatedFiatAmount(trade: trade, fiat: fiatAmount,
                                    fee: platformFee,
                                    price: trade.price ?? 0,
                                    reservedBalance: reservedBalance,
                                    type: currentType)
    }
    
    private func resetError() {
        errorView.update(isHidden: true)
        currentError = nil
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
            fiatAmountView,
            reserverdInfoLine,
            cryptoAmountInfoLine,
            platformFeeInfoLine,
            youWillLabel,
            resultLabel,
            errorView,
            submitButton
        ])
        
      reserverdInfoLine.setup(title: localize(L.P2p.Create.Order.reserved), value: "\(reservedBalance.formatted(fractionPart: 3)) \(coinCode)")
      cryptoAmountInfoLine.setup(title: localize(L.P2p.Crypto.Amount.title), value: "0 \(coinCode)")
      platformFeeInfoLine.setup(title: localize(L.P2p.Create.Order.Platform.fee), value: "0 \(coinCode)")
      
      
      youWillLabel.text = currentType == .sell ? localize(L.P2p.Create.Order.You.Will.send) : localize(L.P2p.Create.Order.You.Will.get)
      
      resultLabel.text = "0 BTC"
      
        submitButton.addTarget(self, action: #selector(createOrder), for: .touchUpInside)
        errorView.update(isHidden: true)
    }
    
    
  
  func setup(trade: Trade, platformFee: Double, reservedBalance: Double, type: TradeType) {
        self.currentType = type
        self.reservedBalance = reservedBalance
        self.tradePrice = trade.price ?? 0
        self.platformFee = platformFee
        self.coinCode = trade.coin ?? ""
        self.trade = trade
    }
    
    @objc private func createOrder() {
        guard currentError == nil else {
            errorView.update(isHidden: false, message: currentError?.message)
            return
        }
        
        delegate?.createOrder(price: tradePrice, cryptoAmount: cryptoAmount, fiatAmount: fiatAmount)
        dismiss(animated: true, completion: nil)
    }
    
    func setupLayout() {
      containerView.snp.makeConstraints {
        $0.left.right.equalToSuperview()
        $0.bottom.equalToSuperview().offset(-300)
        $0.height.equalTo(350)
      }

      fiatAmountView.snp.makeConstraints {
            $0.top.equalToSuperview()
            $0.right.equalToSuperview().offset(-15)
            $0.left.equalTo(containerView.snp.centerX)
       }
      
      reserverdInfoLine.snp.makeConstraints {
        $0.top.equalTo(fiatAmountView.snp.bottom).offset(21)
        $0.right.equalToSuperview().offset(-15)
      }
      
      cryptoAmountInfoLine.snp.makeConstraints {
        $0.top.equalTo(reserverdInfoLine.snp.bottom).offset(10)
        $0.right.equalToSuperview().offset(-15)
      }
      
      platformFeeInfoLine.snp.makeConstraints {
        $0.top.equalTo(cryptoAmountInfoLine.snp.bottom).offset(10)
        $0.right.equalToSuperview().offset(-15)
      }
      
      youWillLabel.snp.makeConstraints {
        $0.top.equalTo(platformFeeInfoLine.snp.bottom).offset(20)
        $0.right.equalToSuperview().offset(-15)
      }
      
      resultLabel.snp.makeConstraints {
        $0.top.equalTo(youWillLabel.snp.bottom).offset(20)
        $0.right.equalToSuperview().offset(-15)
      }

      errorView.snp.makeConstraints {
        $0.top.equalTo(resultLabel.snp.bottom)
        $0.height.equalTo(20)
        $0.left.right.equalToSuperview()
      }
        
        submitButton.snp.makeConstraints {
            $0.left.equalToSuperview().offset(15)
            $0.right.equalToSuperview().offset(-15)
            $0.height.equalTo(50)
            $0.bottom.equalToSuperview().offset(-40)
        }
        
        
    }
    
}

extension P2PCreateOrderPopupViewController: P2PCreateOrderPresenterOutput {
  func updated(crypto: String, fee: String, total: String, error: P2PCreateOrderValidationError?) {
        cryptoAmountInfoLine.update(value: "\(crypto) \(coinCode)")
        platformFeeInfoLine.update(value: "\(fee) \(coinCode)")
        resultLabel.text = "\(total) \(coinCode)"
        cryptoAmount = Double(crypto) ?? 0
        fiatAmount = Double(fiatAmountView.fiatTextField.text ?? "0") ?? 0
        currentError = error
    }
}

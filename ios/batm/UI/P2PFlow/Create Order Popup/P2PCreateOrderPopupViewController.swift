import UIKit
import SnapKit
import MaterialComponents

protocol P2PCreateOrderPopupViewControllerDelegate: AnyObject {
    func createOrder(price: Double, cryptoAmount: Double, fiatAmount: Double)
}

class P2PCreateOrderPopupViewController: UIViewController {
    
    private let containerView = UIView()
    private let orderAmountView = P2PCreateOrderAmountView()
    private var tradePrice: Double = 0
    private var platformFee: Double = 0
    private let presenter = P2PCreateOrderPresenter()
    private var coinCode = ""
    private let errorView = P2PInlineErrorView()
    private var cryptoAmount: Double = 0
    private var fiatAmount: Double = 0
    private var trade: Trade?
    private var currentError: P2PCreateOrderValidationError?
    
    weak var delegate: P2PCreateOrderPopupViewControllerDelegate?
    private let submitButton = MDCButton.submit
    private var reservedBalance: Double = 0
    
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
        guard let fiatAmount = textField.text, let trade = trade else { return }
        resetError()
        presenter.updatedFiatAmount(trade: trade, fiat: fiatAmount,
                                    fee: platformFee,
                                    price: trade.price ?? 0,
                                    reservedBalance: reservedBalance)
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
            orderAmountView,
            errorView,
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
        errorView.update(isHidden: true)
    }
    
    
    func setup(trade: Trade, platformFee: Double, reservedBalance: Double) {
        self.reservedBalance = reservedBalance
        self.tradePrice = trade.price ?? 0
        self.platformFee = platformFee
        self.coinCode = trade.coin ?? ""
        self.trade = trade
        orderAmountView.cryptoAmountValue.text = "0 \(coinCode)"
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
            $0.top.equalTo(errorView.snp.bottom)
            $0.centerX.equalToSuperview()
        }
        
        submitButton.snp.makeConstraints {
            $0.top.equalTo(platformFeeStackView.snp.bottom).offset(25)
            $0.left.equalToSuperview().offset(15)
            $0.right.equalToSuperview().offset(-15)
            $0.height.equalTo(50)
        }
        
        errorView.snp.makeConstraints {
            $0.top.equalTo(orderAmountView.snp.bottom)
            $0.height.equalTo(20)
            $0.left.right.equalToSuperview()
        }
        
    }
    
}

extension P2PCreateOrderPopupViewController: P2PCreateOrderPresenterOutput {
    func updated(crypto: String, fee: String, error: P2PCreateOrderValidationError?) {
        orderAmountView.cryptoAmountValue.text = "\(crypto) \(coinCode)"
        platformFeeValueLabel.text = fee
        cryptoAmount = Double(crypto) ?? 0
        fiatAmount = Double(orderAmountView.fiatTextField.text ?? "0") ?? 0
        currentError = error
    }
}

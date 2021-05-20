import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

struct P2PCreateTradeDataModel: Encodable {
    let type: Int
    let coin: String
    let price: Double
    let minLimit: Int
    let maxLimit: Int
    let paymentMethods: String
    let terms: String
  
  var dictionary: [String: Any] {
        return (try? JSONSerialization.jsonObject(with: JSONEncoder().encode(self))) as? [String: Any] ?? [:]
    }
}

protocol P2PCreateTradeViewControllerDelegate: AnyObject {
   func didSelectedSubmit(data: P2PCreateTradeDataModel)
}

class P2PCreateTradeViewController: UIViewController {

    var selectedType: P2PSellBuyViewType = .buy
    var minRange: Int = 100
    var maxRange: Int = 10000 {
        didSet {
            calculateFee()
        }
    }
    
    private var currentPrice: Double = 0 {
        didSet {
            calculateFee()
        }
    }
    
    private var balance: CoinsBalance
    private var payments: [TradePaymentMethods]
    let submitButton = MDCButton.submit
    weak var delegate: P2PCreateTradeViewControllerDelegate?
    
    init(balance: CoinsBalance, payments: [TradePaymentMethods], delegate: P2PCreateTradeViewControllerDelegate) {
        self.balance = balance
        self.payments = payments
        self.delegate = delegate
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private let scrollView = UIScrollView()

    private lazy var stackView: UIStackView = {
        let stack = UIStackView()
        stack.axis = .vertical
        return stack
    }()
    
    private let tradeTypeHeader = P2PSectionHeaderView()
    
    private let selectTradeTypeView = P2PCreateTradeSellBuyView()
    
    private let tradeSeparator = P2PSeparatorView()
    private let coinExchangeView = P2PSelectCoinView()
    private let coinExchangeSeparator = P2PSeparatorView()
    
    private let paymentMethodsHeader = P2PSectionHeaderView()
    private let paymentMethodsView = P2PTagContainerView(width: UIScreen.main.bounds.size.width - 20)
    private let paymentMethodSeparator = P2PSeparatorView()
    private let limitsHeader = P2PSectionHeaderView()
    private let limitsView = P2PCreateTradeLimitsView()
    private let limitsSeparator = P2PSeparatorView()
    
    private let termsSeparator = P2PSeparatorView()
    private let termsHeader = P2PSectionHeaderBottomView()
    lazy var termsTextField: MDCMultilineTextField = {
           let field = MDCMultilineTextField.default
           field.borderView = nil
           
           return field
       }()
    
    var termsTextFieldController: ThemedTextInputControllerOutlinedTextArea?
    private var emptyFooterView = UIView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = localize(L.P2p.Trade.Create.Vc.title)
        setupUI()
        setupLayout()
        bind()
      tradeTypeHeader.update(title: localize(L.P2p.TradeType.title))
        addNotificationObserver()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        removeNotificationObserver()
    }
    
    private func setupUI() {
        view.backgroundColor = .white
      paymentMethodsHeader.update(title: localize(L.P2p.Payment.Methods.title))
        setupPaymentMethodsView(payments: payments)
        limitsView.setup(range: [CGFloat(minRange), CGFloat(maxRange)], measureString: "", isMeasurePosistionLast: false)
      limitsHeader.update(title: localize(L.P2p.Limits.title))
      termsHeader.update(title: localize(L.P2p.Terms.title))
        termsTextFieldController = ThemedTextInputControllerOutlinedTextArea(textInput: termsTextField)
      termsTextFieldController?.placeholderText = localize(L.P2p.Terms.placeholder)
        termsTextFieldController?.minimumLines = 3
        submitButton.addTarget(self, action: #selector(createTrade), for: .touchUpInside)
        selectTradeTypeView.delegate = self
        view.addSubviews([
            scrollView,
        ])
        
        scrollView.addSubview(stackView)
        scrollView.keyboardDismissMode = .onDrag
        
        let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(didTapView))
        stackView.addGestureRecognizer(tapRecognizer)
        
        
        stackView.addArrangedSubviews([
            tradeTypeHeader,
            selectTradeTypeView,
            tradeSeparator,
            coinExchangeView,
            coinExchangeSeparator,
            paymentMethodsHeader,
            paymentMethodsView,
            paymentMethodSeparator,
            limitsHeader,
            limitsView,
            termsSeparator,
            termsHeader,
            termsTextField,
            submitButton,
            emptyFooterView
        ])
        
        coinExchangeView.configure(for: .bitcoin, coins: balance.coins.map { $0.type })
        coinExchangeView.amountTextField.addTarget(self, action: #selector(priceChanged(_:)), for: .editingChanged)
        
        limitsView.selectedMinRange { [weak self] minRange in
            self?.minRange = minRange
        } maxRange: { [weak self] maxRange in
            self?.maxRange = maxRange
        }
        
    }
    
    @objc func priceChanged(_ textField: UITextField) {
        guard let price = Double(textField.text ?? "") else { return }
        currentPrice = price
    }
    
    func calculateFee() {
        guard currentPrice > 0 else { return }
        let value = Double(maxRange) / currentPrice
        limitsView.feeLabel.text = "~ \(value.coinFormatted)"
    }
    
    private func addNotificationObserver() {
        let notificationCenter = NotificationCenter.default
        notificationCenter.addObserver(self, selector: #selector(adjustForKeyboard), name: UIResponder.keyboardWillHideNotification, object: nil)
        notificationCenter.addObserver(self, selector: #selector(adjustForKeyboard), name: UIResponder.keyboardWillChangeFrameNotification, object: nil)
    }
    
    private func removeNotificationObserver() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc func adjustForKeyboard(notification: Notification) {
        guard termsTextField.isFirstResponder == true else { return }
        guard let keyboardValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue else { return }
        let keyboardScreenEndFrame = keyboardValue.cgRectValue
        let keyboardViewEndFrame = view.convert(keyboardScreenEndFrame, from: view.window)

        if notification.name == UIResponder.keyboardWillHideNotification {
            scrollView.contentInset = .zero
        } else {
            scrollView.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: keyboardViewEndFrame.height - view.safeAreaInsets.bottom, right: 0)
            let bottomOffset = CGPoint(x: 0, y: scrollView.contentSize.height - scrollView.frame.size.height + scrollView.contentInset.bottom);
            scrollView.setContentOffset(bottomOffset, animated: true)
        }

    }
    
    @objc private func didTapView() {
        view.endEditing(true)
    }
    
    @objc private func createTrade() {
        let selectedPaymentTitles = paymentMethodsView.selectedTitles()
        let methods = selectedPaymentTitles.compactMap{TradePaymentMethods(method: $0)?.rawValue}.map { String($0)}
        let paymentMethods = methods.joined(separator: ",")
        
        let data = P2PCreateTradeDataModel(type: selectedType.rawValue,
                                coin: coinExchangeView.coinType?.code ?? "",
                                price: Double(coinExchangeView.amountTextField.text ?? "") ?? 0 ,
                                minLimit: minRange ?? 0,
                                maxLimit: maxRange ?? 0,
                                paymentMethods: paymentMethods,
                                terms: termsTextField.text ?? "")

        delegate?.didSelectedSubmit(data: data)
    }
    
    private func setupLayout() {
        let separatorHeight = 1 / UIScreen.main.scale
        
        scrollView.snp.makeConstraints {
            $0.centerX.equalTo(view.snp.centerX)
            $0.width.equalToSuperview()
            $0.top.equalToSuperview()
            $0.bottom.equalToSuperview()
        }
        
        stackView.snp.makeConstraints {
            $0.top.equalToSuperview()
            $0.bottom.equalToSuperview()
            $0.right.equalToSuperview()
            $0.left.equalToSuperview()
            $0.width.equalToSuperview()
        }
        
        tradeTypeHeader.snp.makeConstraints {
            $0.top.right.left.equalToSuperview()
            $0.height.equalTo(65)
        }
        
        selectTradeTypeView.snp.makeConstraints {
            $0.top.equalTo(tradeTypeHeader.snp.bottom)
            $0.right.equalToSuperview()
            $0.height.equalTo(65)
            $0.left.equalToSuperview().offset(15)
        }
        
        tradeSeparator.snp.makeConstraints {
            $0.top.equalTo(selectTradeTypeView.snp.bottom)
            $0.height.equalTo(separatorHeight)
            $0.left.equalToSuperview().offset(15)
            $0.right.equalToSuperview().offset(-15)
        }
        
        coinExchangeView.snp.makeConstraints {
            $0.top.equalTo(tradeSeparator.snp.bottom)
            $0.left.right.equalToSuperview()
            $0.height.equalTo(100)
        }
        
        coinExchangeSeparator.snp.makeConstraints {
            $0.top.equalTo(coinExchangeView.snp.bottom)
            $0.height.equalTo(separatorHeight)
            $0.left.equalToSuperview().offset(15)
            $0.right.equalToSuperview().offset(-15)
        }
        
        paymentMethodsHeader.snp.remakeConstraints {
            $0.top.equalTo(coinExchangeSeparator.snp.bottom)
            $0.right.left.equalToSuperview()
            $0.height.equalTo(65)
        }

        paymentMethodsView.snp.makeConstraints {
            $0.top.equalTo(paymentMethodsHeader.snp.bottom)
            $0.right.left.equalToSuperview()
        }

        paymentMethodSeparator.snp.makeConstraints {
            $0.top.equalTo(paymentMethodsView.snp.bottom)
            $0.height.equalTo(separatorHeight)
            $0.left.equalToSuperview().offset(15)
            $0.right.equalToSuperview().offset(-15)
        }
        
        limitsHeader.snp.remakeConstraints {
            $0.top.equalTo(paymentMethodSeparator.snp.bottom)
            $0.right.left.equalToSuperview()
            $0.height.equalTo(65)
        }
        
        limitsView.snp.makeConstraints {
            $0.left.equalToSuperview().offset(30)
            $0.right.equalToSuperview().offset(-30)
            $0.top.equalTo(limitsHeader.snp.bottom)
            $0.height.equalTo(120)
        }
        
        termsSeparator.snp.makeConstraints {
            $0.top.equalTo(limitsView.snp.bottom).offset(20)
            $0.height.equalTo(separatorHeight)
            $0.left.equalToSuperview().offset(15)
            $0.right.equalToSuperview().offset(-15)
        }
        
        termsHeader.snp.remakeConstraints {
            $0.top.equalTo(termsSeparator.snp.bottom)
            $0.right.left.equalToSuperview()
            $0.height.equalTo(45)
        }
        
        termsTextField.snp.makeConstraints {
            $0.top.equalTo(termsHeader.snp.bottom)
            $0.left.equalToSuperview().offset(5)
            $0.right.equalToSuperview().offset(-5)
            $0.height.equalTo(105)
        }
        
        submitButton.snp.makeConstraints {
            $0.top.equalTo(termsTextField.snp.bottom)
            $0.height.equalTo(50)
            $0.left.right.equalToSuperview().inset(15)
        }
        
        emptyFooterView.snp.makeConstraints {
            $0.top.equalTo(submitButton.snp.bottom)
            $0.left.right.equalToSuperview()
            $0.height.equalTo(40)
        }
    }
    
    private func bind() {
        
        guard let firstBalance = balance.coins.first else { return }
        
        coinExchangeView.setCoinBalance(firstBalance)
        
        coinExchangeView.didSelectPickerRow.asObservable().subscribe { [unowned self] type in
            if let selectedbalance = balance.coins.first(where: { $0.type == type.element }) {
                self.coinExchangeView.setCoinBalance(selectedbalance)
            }
        }.disposed(by: disposeBag)
        
    }
    
    private func setupPaymentMethodsView(payments: [TradePaymentMethods]) {
        var methods = [P2PTagView]()
        for method in payments {
            let tag = P2PTagView()
            tag.update(image: method.image, title: method.title)
            tag.layoutIfNeeded()
            methods.append(tag)
        }
        
        paymentMethodsView.update(tags: methods)
    }
}

extension P2PCreateTradeViewController: P2PCreateTradeSellBuyViewDelegate {
    func didSelectedType(_ type: P2PSellBuyViewType) {
        selectedType = type
    }
}

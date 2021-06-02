import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

struct P2PCreateTradeDataModel: Encodable {
    let type: Int
    let coin: String
    let price: Double
    let minLimit: Double
    let maxLimit: Double
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

    let trades: Trades
    let userId: Int
    var selectedType: P2PSellBuyViewType = .buy
    var minRange: Double = 100
    var maxRange: Double = 1000 {
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
    private let formValidator = P2PCreateTradeFormValidator()
    
    let submitButton = MDCButton.submit
    weak var delegate: P2PCreateTradeViewControllerDelegate?
    
    init(trades: Trades,
         userId: Int,
         balance: CoinsBalance,
         payments: [TradePaymentMethods],
         delegate: P2PCreateTradeViewControllerDelegate) {
        self.balance = balance
        self.payments = payments
        self.delegate = delegate
        self.trades = trades
        self.userId = userId
        
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
    private let selectTradeInlineError = P2PFormInlineErrorView()
    
    private let tradeSeparator = P2PSeparatorView()
    private let coinExchangeView = P2PSelectCoinView()
    private let coinInlineError = P2PFormInlineErrorView()
    private let coinExchangeSeparator = P2PSeparatorView()
    
    private let paymentMethodsHeader = P2PSectionHeaderView()
    private let paymentMethodsView = P2PTagContainerView(width: UIScreen.main.bounds.size.width - 20)
    private let paymentMethodsInlineError = P2PFormInlineErrorView()
    private let paymentMethodSeparator = P2PSeparatorView()
    
    private let limitsHeader = P2PSectionHeaderView()
    private let limitsView = P2PCreateTradeLimitsView()
    private let limitInlineError = P2PFormInlineErrorView()
    private let limitsSeparator = P2PSeparatorView()
    private var selectedCointype: CustomCoinType = .bitcoin
    
    lazy var termsTextField: MDCMultilineTextField = {
           let field = MDCMultilineTextField.default
           field.borderView = nil
           
           return field
       }()
    
    private let termsInlineError = P2PFormInlineErrorView()
    private let termsSeparator = P2PSeparatorView()
    var termsTextFieldController: ThemedTextInputControllerOutlinedTextArea?
    private var emptyFooterView = UIView()
    
    private let coinValidator = P2PCreateTradeCoinsValidator()
    private let paymentMethodValidator = P2PCreateTradePaymentValidator()
    private let limitValidator = P2PCreateTradeLimitsValidator()
    private let termsValidator = P2PCreateTradeTermsValidator()
    private let typeValidator = P2PCreateTradeTypeValidator()
    
    var prevResponder: UIView?
    
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
        limitValidator.update(min: Double(minRange))
        limitValidator.update(max: Double(maxRange))
        
        limitsView.update(isUserInteractionEnabled: true, keyboardType: .decimalPad)
        
      limitsHeader.update(title: localize(L.P2p.Limits.title))
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
            selectTradeInlineError,
            tradeSeparator,
            coinExchangeView,
            coinInlineError,
            coinExchangeSeparator,
            paymentMethodsHeader,
            paymentMethodsView,
            paymentMethodsInlineError,
            paymentMethodSeparator,
            limitsHeader,
            limitsView,
            limitInlineError,
            termsSeparator,
            termsTextField,
            termsInlineError,
            submitButton,
            emptyFooterView
        ])
        
        coinExchangeView.configure(for: .bitcoin, coins: balance.coins.map { $0.type })
        coinExchangeView.amountTextField.addTarget(self, action: #selector(priceChanged(_:)), for: .editingChanged)
        
        limitsView.selectedMinRange { [weak self] minRange in
            self?.minRange = minRange
            self?.limitValidator.update(min: Double(minRange))
            self?.limitValidator.check()
        } maxRange: { [weak self] maxRange in
            self?.maxRange = maxRange
            self?.limitValidator.update(max: Double(maxRange))
            self?.limitValidator.check()
        }
        
        [selectTradeInlineError,
         coinInlineError,
         paymentMethodsInlineError,
         limitInlineError,
         termsInlineError].forEach{ $0.isHidden = true }
        
        coinValidator.setup(trades: trades.trades, userId: userId)
        formValidator.register(view: coinInlineError, validator: coinValidator)
        formValidator.register(view: paymentMethodsInlineError, validator: paymentMethodValidator)
        formValidator.register(view: limitInlineError, validator: limitValidator)
        formValidator.register(view: termsInlineError, validator: termsValidator)
        formValidator.register(view: selectTradeInlineError, validator: typeValidator)
        
        coinExchangeView.amountTextField.addTarget(self, action: #selector(amountDidChange(_:)), for: .editingChanged)
        coinExchangeView.amountTextField.deleteDelegate = self
       
        termsTextField.rx.text
            .asDriver()
            .filterNil()
            .filterEmpty()
            .do { [weak self] (value) in
            self?.termsValidator.update(terms: value)
            self?.termsValidator.check()
        }.asObservable()
        .subscribe()
        .disposed(by: disposeBag)
    }
    
    @objc func amountDidChange(_ textField: UITextField) {
    
        guard let value = Double(textField.text ?? "") else { return }
        coinValidator.update(coins: value)
        coinValidator.check()
        limitValidator.update(price: value)
        limitValidator.check()
    }
    
    @objc func priceChanged(_ textField: UITextField) {
        guard let price = Double(textField.text ?? "") else { return }
        currentPrice = price
    }
    
    func calculateFee() {
        guard currentPrice > 0 else {
            limitsView.feeLabel.text = "0 \(selectedCointype.code)"
            return
        }
        let value = Double(maxRange) / currentPrice
        limitsView.feeLabel.text = "\(value.coinFormatted) \(selectedCointype.code)"
    }
    
    private func addNotificationObserver() {
        let notificationCenter = NotificationCenter.default
        notificationCenter.addObserver(self, selector: #selector(adjustForKeyboard), name: UIResponder.keyboardWillShowNotification, object: nil)
        notificationCenter.addObserver(self, selector: #selector(adjustForKeyboard), name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    
    private func removeNotificationObserver() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc func adjustForKeyboard(notification: Notification) {
        guard coinExchangeView.amountTextField.isFirstResponder == false else { return }
        let currentResponder = [limitsView.fromField.textField, limitsView.toField.textField, termsTextField].first(where: {$0.isFirstResponder == true })
        let responderGroup = [limitsView.fromField.textField as UIView , limitsView.toField.textField as UIView]
        guard let keyboardValue = notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue else { return }
        let keyboardScreenEndFrame = keyboardValue.cgRectValue
        let keyboardViewEndFrame = view.convert(keyboardScreenEndFrame, from: view.window)
        if notification.name == UIResponder.keyboardWillHideNotification {
            
            scrollView.setContentOffset(.zero, animated: true)
            UIView.animate(withDuration: 0.2) { [weak self] in
                self?.scrollView.contentInset = .zero
            }
            prevResponder = nil
        } else {
            if let prev = prevResponder, responderGroup.contains(prev), currentResponder != termsTextField { return }
            UIView.animate(withDuration: 0.2) { [weak self] in
                self?.scrollView.contentInset = UIEdgeInsets(top: 0, left: 0, bottom: keyboardViewEndFrame.height - CGFloat(self?.view.safeAreaInsets.bottom ?? 0), right: 0)
            }
            let bottomOffset = CGPoint(x: 0, y: scrollView.contentSize.height - scrollView.frame.size.height + scrollView.contentInset.bottom);
            scrollView.setContentOffset((bottomOffset), animated: true)
        }
        prevResponder = currentResponder
    }

    @objc private func didTapView() {
        view.endEditing(true)
        scrollView.setContentOffset(.zero, animated: true)
        UIView.animate(withDuration: 0.2) { [weak self] in
            self?.scrollView.contentInset = .zero
        }
        prevResponder = nil
    }
    
    @objc private func createTrade() {
        
        formValidator.validate()
        
        guard formValidator.isFormValid() == true else { return }
        
        let selectedPaymentTitles = paymentMethodsView.selectedTitles()
        let methods = selectedPaymentTitles.compactMap{TradePaymentMethods(method: $0)?.rawValue}.map { String($0)}
        let paymentMethods = methods.joined(separator: ",")
        
        let data = P2PCreateTradeDataModel(type: selectedType.rawValue,
                                coin: coinExchangeView.coinType?.code ?? "",
                                price: Double(coinExchangeView.amountTextField.text ?? "") ?? 0 ,
                                minLimit: minRange,
                                maxLimit: maxRange,
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
        
        selectTradeInlineError.snp.makeConstraints {
            $0.left.equalToSuperview().offset(15)
            $0.height.equalTo(30)
        }
        
        tradeSeparator.snp.makeConstraints {
            $0.height.equalTo(separatorHeight)
            $0.left.equalToSuperview().offset(15)
            $0.right.equalToSuperview().offset(-15)
        }
        
        coinExchangeView.snp.makeConstraints {
            $0.top.equalTo(tradeSeparator.snp.bottom)
            $0.left.right.equalToSuperview()
            $0.height.equalTo(100)
        }
        
        coinInlineError.snp.makeConstraints {
            $0.left.equalToSuperview().offset(15)
            $0.height.equalTo(30)
        }
        
        coinExchangeSeparator.snp.makeConstraints {
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

        paymentMethodsInlineError.snp.makeConstraints {
            $0.left.equalToSuperview().offset(15)
            $0.height.equalTo(30)
        }
        
        paymentMethodSeparator.snp.makeConstraints {
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
            $0.height.equalTo(100)
        }
        
        limitInlineError.snp.makeConstraints {
            $0.left.equalToSuperview().offset(15)
            $0.height.equalTo(30)
        }
        
        termsSeparator.snp.makeConstraints {
            $0.height.equalTo(separatorHeight)
            $0.left.equalToSuperview().offset(15)
            $0.right.equalToSuperview().offset(-15)
        }
        
        termsTextField.snp.makeConstraints {
            $0.top.equalTo(termsSeparator.snp.bottom)
            $0.left.equalToSuperview().offset(5)
            $0.right.equalToSuperview().offset(-5)
            $0.height.equalTo(105)
        }
        
        termsInlineError.snp.makeConstraints {
            $0.left.equalToSuperview().offset(15)
            $0.height.equalTo(30)
        }
        
        submitButton.snp.makeConstraints {
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
        
        if let defaultReserved = balance.coins.first(where: { $0.type == .bitcoin }) {
            limitValidator.update(reservedBalance: defaultReserved.reservedBalance.doubleValue)
        }
        
        coinExchangeView.setCoinBalance(firstBalance)
        
        coinExchangeView.didSelectPickerRow.asObservable().subscribe { [unowned self] type in
            if let selectedbalance = balance.coins.first(where: { $0.type == type.element }) {
                self.selectedCointype = selectedbalance.type
                self.coinExchangeView.setCoinBalance(selectedbalance)
                self.coinValidator.update(coinType: selectedbalance.type)
                self.coinValidator.check()
                self.limitValidator.update(reservedBalance: selectedbalance.reservedBalance.doubleValue)
                self.limitValidator.check()
            }
        }.disposed(by: disposeBag)
        
    }
    
    private func setupPaymentMethodsView(payments: [TradePaymentMethods]) {
        var methods = [P2PTagView]()
        for method in payments {
            let tag = P2PTagView()
            tag.delegte = self
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
        coinValidator.update(tradeType: type)
        coinValidator.check()
        limitValidator.update(tradeType: type)
        limitValidator.check()
        typeValidator.update(type: type)
        typeValidator.check()
    }
}

extension P2PCreateTradeViewController: P2PTagViewDelegate {
    func didTapTag(view: P2PTagView) {
        paymentMethodValidator.update(paymentView: view)
        paymentMethodValidator.check()
    }
}

extension P2PCreateTradeViewController: P2PTextFieldDelegate {
    func textFieldDidDelete(_ textField: UITextField) {
        if textField == coinExchangeView.amountTextField {
            let value = Double(textField.text ?? "0") ?? 0
            coinValidator.update(coins: value)
            coinValidator.check()
            limitValidator.update(price: value)
            limitValidator.check()
        }
    }
}

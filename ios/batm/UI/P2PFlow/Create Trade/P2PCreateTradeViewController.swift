import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

class P2PCreateTradeViewController: UIViewController {

    private var balance: CoinsBalance
    private var payments: [TradePaymentMethods]
    let submitButton = MDCButton.submit
    
    init(balance: CoinsBalance, payments: [TradePaymentMethods]) {
        self.balance = balance
        self.payments = payments
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
    private let termsHeader = P2PSectionHeaderView()
    lazy var termsTextField: MDCMultilineTextField = {
           let field = MDCMultilineTextField.default
           field.borderView = nil
           
           return field
       }()
    
    var termsTextFieldController: ThemedTextInputControllerOutlinedTextArea?
    private var emptyFooterView = UIView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupLayout()
        bind()
        tradeTypeHeader.update(title: "Trade Type")
    }
    
    private func setupUI() {
        view.backgroundColor = .white
        paymentMethodsHeader.update(title: "Payment methods")
        setupPaymentMethodsView(payments: payments)
        limitsView.setup(range: [100, 10000], measureString: "$ ", isMeasurePosistionLast: false)
        limitsHeader.update(title: "Limits")
        termsHeader.update(title: "Terms")
        termsTextFieldController = ThemedTextInputControllerOutlinedTextArea(textInput: termsTextField)
        termsTextFieldController?.placeholderText = "Type your terms or comments"
        termsTextFieldController?.minimumLines = 3
        
        
        view.addSubviews([
            scrollView,
        ])
        
        scrollView.addSubview(stackView)
        
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
        
        limitsView.selectedMinRange { [weak self] minRange in
//            self?.minRange = minRange
        } maxRange: { [weak self] maxRange in
//            self?.maxRange = maxRange
        }

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
            $0.height.equalTo(65)
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

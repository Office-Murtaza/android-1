import UIKit
import RxCocoa
import RxSwift
import SnapKit
import MaterialComponents

struct P2PEditTradeDataModel: Encodable {
  let id: String
  let price: Double
  let minLimit: Int
  let maxLimit: Int
  let paymentMethods: String
  let terms: String
  
  var dictionary: [String: Any] {
    return (try? JSONSerialization.jsonObject(with: JSONEncoder().encode(self))) as? [String: Any] ?? [:]
  }
}

protocol P2PEditTradeViewControllerDelegate: class {
  func didSelectEdit(data: P2PEditTradeDataModel)
}

class P2PEditTradeViewController: UIViewController {
  
  let selectedType: P2PSellBuyViewType
  var minRange: Int?
  var maxRange: Int?
  
  let currentModel: P2PEditTradeDataModel
  
  private var balance: CoinsBalance
  private var payments: [TradePaymentMethods]
  private let coinType: String
  
  let editButton = MDCButton.edit
  weak var delegate: P2PEditTradeViewControllerDelegate?
  
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
  
  private let scrollView = UIScrollView()
  
  init(balance: CoinsBalance,
       payments: [TradePaymentMethods],
       delegate: P2PEditTradeViewControllerDelegate,
       editModel: P2PEditTradeDataModel,
       coin: String,
       tradeType: P2PSellBuyViewType) {
    self.balance = balance
    self.payments = payments
    self.delegate = delegate
    self.currentModel = editModel
    self.coinType = coin
    self.selectedType = tradeType
    minRange = editModel.minLimit
    maxRange = editModel.maxLimit
    
    super.init(nibName: nil, bundle: nil)

    self.termsTextField.text = editModel.terms
    
  }
  
  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  
  override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    setupLayout()
    bind()
    tradeTypeHeader.update(title: "Trade Type")
    addNotificationObserver()
  }
  
  override func viewWillDisappear(_ animated: Bool) {
    super.viewWillDisappear(animated)
    removeNotificationObserver()
  }
  
  private func setupUI() {
    view.backgroundColor = .white
    paymentMethodsHeader.update(title: "Payment methods")
    setupPaymentMethodsView(payments: payments)
    
    limitsView.setup(range: [100, 10000], measureString: "$ ", isMeasurePosistionLast: false)
  
    if currentModel.maxLimit != 0 {
      limitsView.distanceSlider.value = [CGFloat(currentModel.minLimit), CGFloat(currentModel.maxLimit)]
      limitsView.setInitFieldsValues(from: CGFloat(currentModel.minLimit), to: CGFloat(currentModel.maxLimit))
    }
    
    limitsHeader.update(title: "Limits")
    
    termsHeader.update(title: "Terms")
    
    termsTextFieldController = ThemedTextInputControllerOutlinedTextArea(textInput: termsTextField)
    termsTextFieldController?.placeholderText = "Type your terms or comments"
    termsTextFieldController?.minimumLines = 3
    
    editButton.addTarget(self, action: #selector(editTrade), for: .touchUpInside)
    selectTradeTypeView.setActive(type: selectedType)
    
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
      editButton,
      emptyFooterView
    ])
    
    coinExchangeView.configure(for: .bitcoin, coins: balance.coins.map { $0.type })
    
    
    limitsView.selectedMinRange { [weak self] minRange in
      self?.minRange = minRange
    } maxRange: { [weak self] maxRange in
      self?.maxRange = maxRange
    }
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
  
  @objc private func editTrade() {
    let selectedPaymentTitles = paymentMethodsView.selectedTitles()
    let methods = selectedPaymentTitles.compactMap{TradePaymentMethods(method: $0)?.rawValue}.map { String($0)}
    let paymentMethods = methods.joined(separator: ",")

    let data = P2PEditTradeDataModel(id: currentModel.id,
                                     price: Double(coinExchangeView.amountTextField.text ?? "") ?? 0,
                                     minLimit: minRange ?? 0,
                                     maxLimit: maxRange ?? 0,
                                     paymentMethods: paymentMethods,
                                     terms: termsTextField.text ?? "")

    delegate?.didSelectEdit(data: data)
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
    
    editButton.snp.makeConstraints {
      $0.top.equalTo(termsTextField.snp.bottom)
      $0.height.equalTo(50)
      $0.left.right.equalToSuperview().inset(15)
    }
    
    emptyFooterView.snp.makeConstraints {
      $0.top.equalTo(editButton.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(40)
    }
  }
  
  private func bind() {
    
    guard let firstBalance = balance.coins.first else { return }
    let currentBalance = balance.coins.first(where: {$0.type.code == coinType})
    coinExchangeView.setCoinBalance(currentBalance ?? firstBalance, amount: "$ \(currentModel.price.formatted() ?? "0")")
    
    coinExchangeView.didSelectPickerRow.asObservable().subscribe { [unowned self] type in
      if let selectedbalance = balance.coins.first(where: { $0.type == type.element }) {
        self.coinExchangeView.setCoinBalance(selectedbalance)
      }
    }.disposed(by: disposeBag)
    
  }
  
  private func setupPaymentMethodsView(payments: [TradePaymentMethods]) {
    var methods = [P2PTagView]()
    
    let selectedMethods = currentModel.paymentMethods.components(separatedBy: ",").compactMap{ Int($0) }.map { TradePaymentMethods(rawValue: $0)}
    
    for method in payments {
      let tag = P2PTagView()
      tag.update(image: method.image, title: method.title)
      tag.layoutIfNeeded()
      if selectedMethods.contains(method) {
        tag.didSelected()
      }
      
      methods.append(tag)
    }
    
    paymentMethodsView.update(tags: methods)
  }
}


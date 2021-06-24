import UIKit

protocol P2POrderDetailsViewControllerDelegate: AnyObject {
  func didTapDistance(order: Order)
  func didTap(type: OrderDetailsActionType, orderModel: MyOrderViewModel)
  func selectedRate(orderModel: MyOrderViewModel, rate: Int)
}

class P2POrderDetailsViewController: UIViewController {
  var order: Order?
  
  weak var delegate: P2POrderDetailsViewControllerDelegate?
  
  private let scrollView = UIScrollView()
  private let coinInfoView = P2PTradeDetailsCoinInfoView()
  private let amountView = P2POrderDetailsAmountView()
  private let amountViewSeparator = P2PSeparatorView()
  private let statusView = P2POrderDetailsStatusView()
  private let statusViewSeparator = P2PSeparatorView()
  private let paymentMethods = P2PTradeDetailsPaymentMethodsView()
  private let paymentViewSeparator = P2PSeparatorView()
  private let tradeRateView =  P2PTradeDetailsRateView()
  private let rateViewSeparator = P2PSeparatorView()
  private let myScoreViewTopSeparator = P2PSeparatorView()
  private let myScoreView = P2POrderDetailsScoreView()
  private let myScoreViewSeparator = P2PSeparatorView()
  private let partnerScoreView = P2POrderDetailsScoreView()
  private let idView = P2POrderDetailsIdView()
  private let idViewSeparator = P2PSeparatorView()
  private var currentDistance: String = ""
  private var currentRate: String = ""
  private var footerView = UIView()
  private let infoMessageView = P2PTradeDetailsTextInfoView()
  
  private var actionSheet = OrderDetailsActionSheet()
  
  var viewModel: MyOrderViewModel?
  
  lazy var stackView: UIStackView = {
    let stack = UIStackView()
    stack.axis = .vertical
    
    return stack
  }()
  
  override func viewDidLoad() {
    super.viewDidLoad()
    title = localize(L.P2p.Order.Details.title)
    view.backgroundColor = .white
    setupUI()
    setupLayout()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    if viewModel?.isNeedPresentRateView == true {
        presentRateController()
    }
  }
  
  func setup(viewModel: MyOrderViewModel, myRate: String) {
    self.viewModel = viewModel
    currentDistance = viewModel.distanceInMiles ?? ""
    currentRate = myRate
    
    let order = viewModel.order
    let infoModel = P2POrderDetailsCoinInfoModel(order: order, sellbuyType: viewModel.currentSellBuyType)
    coinInfoView.update(data: infoModel)
    amountView.update(cryptoAmount:"\(order.cryptoAmount?.coinFormatted ?? "") \(order.coin ?? "")",
                      fiatAmount: "$ \(order.fiatAmount?.coinFormatted ?? "")")
    statusView.update(status: TradeOrderStatus(rawValue: order.status ?? 1) ?? .new)
    setupPaymenMethodsView(order: order)
    
    tradeRateView.setup(markerId: viewModel.makerId,
                        statusImage: nil,
                        rate: viewModel.tradingRate,
                        totalTrades: viewModel.totalTrdades,
                        distance: currentDistance)
    
    myScoreView.setup(title: localize(L.P2p.Order.Details.My.score), score: myRate)
    partnerScoreView.setup(title: localize(L.P2p.Order.Details.Score.From.partner), score: order.makerTradingRate.toString())
    
    myScoreView.isHidden = !(viewModel.orderStatus == .released || viewModel.orderStatus == .solved)
    myScoreViewTopSeparator.isHidden = !(viewModel.orderStatus == .released || viewModel.orderStatus == .solved)
    partnerScoreView.isHidden = !(viewModel.orderStatus == .released || viewModel.orderStatus == .solved)
    
    idView.setup(id: order.id ?? "")
    idView.delegate = self
    tradeRateView.delegate = self
    self.order = order
    
    infoMessageView.update(message: viewModel.terms)
    
    updateActionsView()
  }
  
  func update(order: Order) {
    guard let vm = viewModel else { return }
    vm.update(order: order)
    updateActionsView()
    setup(viewModel: vm, myRate: currentRate)
  }
  
  func updateActionsView() {
    guard let viewModel = viewModel else { return }
    let actionsFactory = OrderDetailsActionsFactory()
    let action = actionsFactory.generateOrderActions(type: viewModel.currentSellBuyType, status: viewModel.orderStatus)
    actionSheet.delegate = self
    actionSheet.update(action: action)
  }
  
  func presentRateController() {
    let controller = P2POrderRateViewController()
    controller.delegate = self
    controller.modalPresentationStyle = .overCurrentContext
    let makerId = viewModel?.order.makerPublicId ?? ""
    let title = String(format: localize(L.P2p.Order.Details.rate), makerId)
    controller.setup(title: title)
    present(controller, animated: true, completion: nil)
  }
  
  func setupPaymenMethodsView(order: Order) {
    
    paymentMethods.removeAll()
    
    guard let methods = order.paymentMethods else { return }
    let images =  methods
      .components(separatedBy: ",")
      .compactMap{ Int($0) }
      .compactMap{ TradePaymentMethods.init(rawValue: $0)?.image }
    
    paymentMethods.update(images: images)
  }
  
  func setupUI() {
    view.addSubviews([
      scrollView,
    ])
    
    scrollView.addSubview(stackView)
    stackView.addArrangedSubviews([
      coinInfoView,
      idView,
      idViewSeparator,
      amountView,
      amountViewSeparator,
      statusView,
      statusViewSeparator,
      paymentMethods,
      paymentViewSeparator,
      tradeRateView,
      rateViewSeparator,
      infoMessageView,
      myScoreViewTopSeparator,
      myScoreView,
      myScoreViewSeparator,
      partnerScoreView,
      actionSheet,
      footerView
    ])
    
  }
  
  func setupLayout() {
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
    
    coinInfoView.snp.makeConstraints {
      $0.top.left.right.equalToSuperview()
      $0.height.equalTo(105)
    }
    
    idView.snp.makeConstraints {
      $0.height.equalTo(56)
    }
    
    idViewSeparator.snp.makeConstraints {
      $0.top.equalTo(idView.snp.bottom)
      $0.height.equalTo(separatorHeight)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    amountView.snp.makeConstraints {
      $0.height.equalTo(75)
    }
    
    amountViewSeparator.snp.makeConstraints {
      $0.top.equalTo(amountView.snp.bottom)
      $0.height.equalTo(separatorHeight)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    statusView.snp.makeConstraints {
      $0.height.equalTo(50)
    }
    
    statusViewSeparator.snp.makeConstraints {
      $0.top.equalTo(statusView.snp.bottom)
      $0.height.equalTo(separatorHeight)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    paymentMethods.snp.makeConstraints {
      $0.top.equalTo(statusViewSeparator.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(56)
    }
    
    paymentViewSeparator.snp.makeConstraints {
      $0.top.equalTo(paymentMethods.snp.bottom)
      $0.height.equalTo(separatorHeight)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    tradeRateView.snp.makeConstraints {
      $0.top.equalTo(paymentViewSeparator.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.height.equalTo(70)
    }
    
    rateViewSeparator.snp.makeConstraints {
      $0.top.equalTo(tradeRateView.snp.bottom)
      $0.height.equalTo(separatorHeight)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    infoMessageView.snp.makeConstraints {
        $0.top.equalTo(rateViewSeparator.snp.bottom)
        $0.left.right.equalToSuperview()
    }
    
    myScoreViewTopSeparator.snp.makeConstraints {
      $0.top.equalTo(infoMessageView.snp.bottom)
      $0.height.equalTo(separatorHeight)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    myScoreView.snp.makeConstraints {
      $0.height.equalTo(58)
    }
    
    myScoreViewSeparator.snp.makeConstraints {
      $0.top.equalTo(myScoreView.snp.bottom)
      $0.height.equalTo(separatorHeight)
      $0.left.equalToSuperview().offset(15)
      $0.right.equalToSuperview().offset(-15)
    }
    
    partnerScoreView.snp.makeConstraints {
      $0.height.equalTo(58)
    }
    
    actionSheet.snp.makeConstraints {
      $0.top.equalTo(partnerScoreView.snp.bottom)
      $0.left.right.equalToSuperview()
    }
    
    footerView.snp.makeConstraints {
      $0.top.equalTo(actionSheet.snp.bottom)
      $0.left.right.equalToSuperview()
      $0.bottom.equalToSuperview()
      $0.height.equalTo(10)
    }
    
  }
}

extension P2POrderDetailsViewController: P2POrderDetailsIdViewDelegate {
  func didSelectedCopy(id: String) {
    view.makeToast(localize(L.Shared.copied))
    UIPasteboard.general.string = id
  }
}

extension P2POrderDetailsViewController: P2PTradeDetailsRateViewDelegate {
  func didTapDistance() {
    guard let order = order else { return }
    delegate?.didTapDistance(order: order)
  }
}

extension P2POrderDetailsViewController: OrderDetailsActionSheetDelegate {
  func didTap(type: OrderDetailsActionType) {
    guard let vm = viewModel else { return }
    delegate?.didTap(type: type, orderModel: vm)
  }
}

extension P2POrderDetailsViewController: P2POrderRateViewControllerDelegate {
  func selectedRate(rate: Int) {
    guard let vm = viewModel else { return }
    delegate?.selectedRate(orderModel: vm, rate: rate)
  }
}

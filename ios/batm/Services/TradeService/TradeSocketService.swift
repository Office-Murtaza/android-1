import Foundation
import RxSwift
import RxCocoa
import Starscream

protocol TradeServiceWebSocket {
  func start()
  func connect()
  func subscribe()
  func unsubscribe() -> Completable
  func disconnect() -> Completable
}

enum TradeServiceError: Error {
  case openConnectionError
  case getPhoneError
  case phoneEmptyDuringUnsubscribe
}

protocol TradeSocketService: TradeServiceWebSocket {
  func getTrade() -> Observable<Trade>
}

class TradeServiceImpl: TradeSocketService {
  
  var socket: WebSocket?
  let api: APIGateway
  let accountStorage: AccountStorage
  let walletStorage: BTMWalletStorage
  let errorService: ErrorService
  
  var account: Account?
  var phone: String?
  
  private var tradeProperty = BehaviorRelay<Trade>(value: Trade.empty)
  private let disposeBag: DisposeBag = DisposeBag()
  private let socketURL: URL
  
  init(api: APIGateway,
       accountStorage: AccountStorage,
       walletStorage: BTMWalletStorage,
       errorService: ErrorService,
       socketURL: URL) {
    self.api = api
    self.accountStorage = accountStorage
    self.walletStorage = walletStorage
    self.errorService = errorService
    self.socketURL = socketURL
    
    subscribeSystemNotifications()
  }
  
  func getTrade() -> Observable<Trade> {
    return tradeProperty.asObservable().filter { trade -> Bool in
      return trade.id != nil
    };
  }
  
  func subscribeSystemNotifications() {
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(handleForeground),
                                           name: UIApplication.willEnterForegroundNotification,
                                           object: nil)
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(handleBackground),
                                           name: UIApplication.didEnterBackgroundNotification,
                                           object:nil)
    
    let  notificationName = Notification.Name(RefreshCredentialsConstants.refreshNotificationName)
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(disconnectAndStart),
                                           name: notificationName,
                                           object:nil)
  }
  
  deinit {
    NotificationCenter.default.removeObserver(self)
  }
  
  @objc func handleForeground() {
     start()
  }
  
  @objc func handleBackground() {
    unsubscribe()
      .andThen(disconnect())
      .subscribe()
      .disposed(by: disposeBag)
  }
}

extension TradeServiceImpl: TradeServiceWebSocket {

  func start() {
    accountStorage.get().subscribe { [weak self] (account) in
      self?.account = account
      guard let requestURL = self?.socketURL else { return }
      self?.socket = WebSocket(request: URLRequest(url: requestURL))
      self?.socket?.delegate = self
      self?.socket?.connect()
    } onError: { _ in
      print("error")
    }.disposed(by: disposeBag)
  }
  
  func connect() {
    guard let accessToken = account?.accessToken else { return }
    let token = "Bearer " + accessToken
    let payload = [
      "Authorization" : token,
      "accept-version" : "1.1",
      "heart-beat" : "1000,1000"
    ]
    let message = ConnectMessageBuilder().build(with: payload)
    socket?.write(string: message)
 }
  
  func subscribe() {

    guard let userId = account?.userId else { return }

    api.getPhoneNumber(userId: userId)
      .observeOn(ConcurrentDispatchQueueScheduler(qos: .background))
      .subscribe { [weak self] (phone) in
        let message = SubscribeMessageBuilder().build(with: [
          "id" : phone.phoneNumber,
          "destination" : "/topic/trade"
        ])
        
        self?.socket?.write(string: message)
     
      } onError: { (error) in
        print("error")
      }.disposed(by: disposeBag)

    
    
 }
  
  func unsubscribe() -> Completable {
    guard let phoneNumber = phone else {
      return Completable.error(BalanceServiceError.phoneEmptyDuringUnsubscribe)
    }
    
    return Completable.create { [weak self] completable in
      let payload = [
        "id" : phoneNumber,
        "destination" : "/topic/trade"
      ];
      
      let message = UnsubscribeMessageBuilder().build(with: payload)
      self?.socket?.write(string: message, completion: {
        completable(.completed)
      })
      return Disposables.create {}
    }
  }
  
  func disconnect() -> Completable {
    return Completable.create { [weak self] completable in
      self?.socket?.disconnect()
      completable(.completed)
      return Disposables.create {}
    }
  }
  
  func handleMessage(_ model: MessageModel) {
    switch model.type {
    case .CONNECTED:
      subscribe()
    case .MESSAGE:
      notify(model)
    case .ERROR, .UNDEFINED:
      handleErrorModel(model)
    default: break
    }
  }
  
  func notify(_ model: MessageModel) {
    guard let json = model.jsonData,
          let trade = Trade(JSON: json) else { return }
    tradeProperty.accept(trade)
  }
  
  private func handleErrorModel(_ model: MessageModel) {
    let messageKey = "message"
    let accessDeniedKey = "Access is denied"
    guard let errorMessage = model.headers[messageKey],
          errorMessage == accessDeniedKey else {
      errorService
        .showError(for: .serverError)
        .subscribe()
        .disposed(by: disposeBag)
      return
    }
    disconnectAndStart()
  }
  
  @objc private func disconnectAndStart() {
    disconnect()
      .subscribe { [weak self] in
        self?.start()
      } onError: { [weak self] (error) in
        guard let self = self else { return }
        self.errorService
          .showError(for: .serverError)
          .subscribe()
          .disposed(by: self.disposeBag)
      }.disposed(by: disposeBag)
  }
}

extension TradeServiceImpl: WebSocketDelegate {
  func didReceive(event: WebSocketEvent, client: WebSocket) {
    switch event {
    case .connected(_): connect()
    case .disconnected(_, _): start()
    case .text(let string):
      let model = SocketResultMessageMapper().mapMessage(string)
      handleMessage(model)
    case .reconnectSuggested(_):
      unsubscribe()
        .andThen(disconnect())
        .subscribe { [weak self] in
          self?.start()
        } onError: { [weak self] _ in
          self?.handleMessage(MessageModel.errorMessage)
        }.disposed(by: disposeBag)
    case .error(_): handleMessage(MessageModel.errorMessage)
    default: break
    }
  }
}
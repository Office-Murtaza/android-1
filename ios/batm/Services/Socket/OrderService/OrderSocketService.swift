import Foundation
import RxSwift
import RxCocoa
import Starscream

protocol OrderServiceWebSocket {
    func start()
    func connect()
    func subscribe()
    func unsubscribe() -> Completable
    func disconnect() -> Completable
}

enum OrderServiceError: Error {
    case openConnectionError
    case getPhoneError
    case phoneEmptyDuringUnsubscribe
}

protocol OrderSocketService: OrderServiceWebSocket {
    func getOrder() -> Observable<Order>
}

class OrderServiceImpl: OrderSocketService {
    
    var socket: WebSocket?
    let api: APIGateway
    let accountStorage: AccountStorage
    let walletStorage: BTMWalletStorage
    let errorService: ErrorService
    
    var account: Account?
    var phone: String?
    
    private var orderProperty = BehaviorRelay<Order>(value: Order.empty)
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
        
    }
    
    func getOrder() -> Observable<Order> {
        return orderProperty.asObservable().filter { order -> Bool in
            return order.id != nil
        }
    }
}

extension OrderServiceImpl: OrderServiceWebSocket {
    
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
            "heart-beat" : "5000,5000"
        ]
        let message = ConnectMessageBuilder().build(with: payload)
        socket?.write(string: message)
    }
    
    func subscribe() {
        guard let userId = account?.userId else { return }
        
        api.getPhoneNumber(userId: userId)
            .observeOn(ConcurrentDispatchQueueScheduler(qos: .background))
            .subscribe { [weak self] phone in
                self?.phone = phone.phoneNumber
                let message = SubscribeMessageBuilder().build(with: [
                    "id" : phone.phoneNumber,
                    "destination" : "/user/queue/order"
                ])
                
                self?.socket?.write(string: message)
            } onError: { (error) in
                print("error")
            }.disposed(by: disposeBag)
    }
    
    func unsubscribe() -> Completable {
        guard let phoneNumber = phone else {
            return Completable.error(OrderServiceError.phoneEmptyDuringUnsubscribe)
        }
        
        return Completable.create { [weak self] completable in
            let payload = [
                "id" : phoneNumber,
                "destination" : "/user/queue/order"
            ]
            
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
        .retry(maxAttempts: 1, delay: 60)
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
              let trade = Order(JSON: json) else { return }
        orderProperty.accept(trade)
    }
    
    private func handleErrorModel(_ model: MessageModel) {
      unsubscribe()
          .andThen(disconnect())
          .subscribe { [weak self] in
              self?.start()
          } onError: { [weak self] _ in
              self?.handleMessage(MessageModel.errorMessage)
          }.disposed(by: disposeBag)
    }
}

extension OrderServiceImpl: WebSocketDelegate {
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

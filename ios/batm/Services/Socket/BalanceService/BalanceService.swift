import Foundation
import RxSwift
import RxCocoa
import Starscream

protocol BalanceServiceWebSocket {
    func start()
    func connect()
    func subscribe()
    func unsubscribe() -> Completable
    func disconnect() -> Completable
}

enum BalanceServiceError: Error {
    case openConnectionError
    case getPhoneError
    case phoneEmptyDuringUnsubscribe
}

protocol BalanceService: BalanceServiceWebSocket {
    func getCoinsBalance() -> Observable<CoinsBalance>
    func getCoinDetails(for coinType: CustomCoinType) -> Observable<CoinDetails?>
    func removeCoinDetails()
}

class BalanceServiceImpl: BalanceService {
    
    var account: Account?
    var phone: String?
    var socket: WebSocket?
    
    let api: APIGateway
    let accountStorage: AccountStorage
    let walletStorage: BTMWalletStorage
    let errorService: ErrorService
    
    private var coinType: CustomCoinType?
    private var balanceProperty = BehaviorRelay<CoinsBalance>(value: CoinsBalance.empty)
    private var detailsProperty = BehaviorRelay<CoinDetails?>(value: .empty)
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
    
    func getCoinsBalance() -> Observable<CoinsBalance> {
        return balanceProperty.asObservable().filter { balance -> Bool in
            return balance.coins.isNotEmpty
        }
    }
    
    func getCoinDetails(for coinType: CustomCoinType) -> Observable<CoinDetails?> {
        balanceProperty.asObservable().subscribe(onNext: { [weak self] in
            self?.detailsProperty.accept($0.coins.first { $0.type == coinType }?.details)
        })
        .disposed(by: disposeBag)
        return detailsProperty.asObservable()
    }
    
    func removeCoinDetails() {
        detailsProperty.accept(nil)
    }
    
    func subscribeSystemNotifications() {
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(handleForeground),
                                               name: UIApplication.willEnterForegroundNotification,
                                               object: nil)
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(handleBackground),
                                               name: UIApplication.didEnterBackgroundNotification,
                                               object: nil)
        let notificationName = Notification.Name(RefreshCredentialsConstants.refreshNotificationName)
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(disconnectAndStart),
                                               name: notificationName,
                                               object: nil)
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

extension BalanceServiceImpl: BalanceServiceWebSocket {
    
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
            .flatMap { [unowned self] (phoneNumber) -> Single<[BTMCoin]> in
                UserDefaultsHelper.userPhoneNumber = phoneNumber.phoneNumber
                self.phone = phoneNumber.phoneNumber
                return self.walletStorage.get()
                    .map { $0.coins.filter { $0.isVisible } }
                    .flatMap{ Single.just($0)}
            }.subscribe { [weak self] coins in
                let activeCoins = coins.map { $0.type.code }.joined(separator: ",")
                guard let phone = self?.phone else { return }
                let payload = [
                    "id" : phone,
                    "destination" : "/user/queue/balance",
                    "coins" : activeCoins
                ]
                let message = SubscribeMessageBuilder().build(with: payload)
                
                self?.socket?.write(string: message)
            } onError: { _ in
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
                "destination" : "/user/queue/balance"
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
              let balance = CoinsBalance(JSON: json) else { return }
        balanceProperty.accept(balance)
    }
    
    private func handleErrorModel(_ model: MessageModel) {
        disconnectAndStart()
    }
    
    @objc private func disconnectAndStart() {
        disconnect()
            .observeOn(ConcurrentDispatchQueueScheduler(qos: .background))
            .retry(maxAttempts: 1, delay: 60)
            .subscribe { [weak self] in
                self?.start()
            }.disposed(by: disposeBag)
    }
}

extension BalanceServiceImpl: WebSocketDelegate {
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

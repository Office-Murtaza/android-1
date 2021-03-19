//
//  TransactionService.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 02.04.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import Foundation
import RxSwift
import RxCocoa
import Starscream

protocol TransactionDetailsServiceWebSocket {
    func start()
    func connect()
    func subscribe()
    func unsubscribe() -> Completable
    func disconnect() -> Completable
}

enum TransactionServiceError: Error {
    case openConnectionError
    case getPhoneError
    case phoneEmptyDuringUnsubscribe
    case phoneEmptyDuringSubscribe
}

protocol TransactionDetailsService: TransactionDetailsServiceWebSocket {
    func getTransactionDetails() -> Observable<TransactionDetails?>
    func removeTransactionDetails()
}

enum TransactionDetailsNotification {
    static let connectTransaction = "connectTransaction"
    static let disconnectTransaction = "disconnectTransaction"
}

class TransactionDetailsServiceImpl: TransactionDetailsService {
    var account: Account?
    var socket: WebSocket?
    
    let api: APIGateway
    let accountStorage: AccountStorage
    let errorService: ErrorService
    
    private var coinType: CustomCoinType?
    private var detailsProperty = BehaviorRelay<TransactionDetails?>(value: nil)
    private let disposeBag: DisposeBag = DisposeBag()
    private let socketURL: URL
    
    init(api: APIGateway,
         accountStorage: AccountStorage,
         errorService: ErrorService,
         socketURL: URL) {
        self.api = api
        self.accountStorage = accountStorage
        self.errorService = errorService
        self.socketURL = socketURL
        
        subscribeSystemNotifications()
    }
    
    func getTransactionDetails() -> Observable<TransactionDetails?> {
        return detailsProperty.asObservable()
    }
    
    func removeTransactionDetails() {
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
        let  connectNotificationName = Notification.Name(TransactionDetailsNotification.connectTransaction)
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(disconnectAndStart),
                                               name: connectNotificationName,
                                               object: nil)
        
        let  disconnectNotificationName = Notification.Name(TransactionDetailsNotification.disconnectTransaction)
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(handleBackground),
                                               name: disconnectNotificationName,
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

extension TransactionDetailsServiceImpl: TransactionDetailsServiceWebSocket {
    func start() {
        accountStorage.get().subscribe { [weak self] account in
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
            "Authorization": token,
            "accept-version": "1.1",
            "heart-beat": "1000,1000"
        ]
        let message = ConnectMessageBuilder().build(with: payload)
        socket?.write(string: message)
    }
    
    func subscribe() {
        guard let phoneNumber = UserDefaultsHelper.userPhoneNumber else {
            handleMessage(MessageModel.errorMessage)
            return
        }
        
        let payload = [
            "id": phoneNumber,
            "destination": "/user/queue/transaction"
        ]
        let message = SubscribeMessageBuilder().build(with: payload)
        socket?.write(string: message)
    }
    
    func unsubscribe() -> Completable {
        guard let phoneNumber = UserDefaultsHelper.userPhoneNumber else {
            return Completable.error(TransactionServiceError.phoneEmptyDuringUnsubscribe)
        }
        
        return Completable.create { [weak self] completable in
            let payload = [
                "id" : phoneNumber,
                "destination" : "/user/queue/transaction"
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
              let details = TransactionDetails(JSON: json) else { return }
        self.detailsProperty.accept(details)
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

extension TransactionDetailsServiceImpl: WebSocketDelegate {
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

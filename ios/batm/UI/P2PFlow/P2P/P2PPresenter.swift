import Foundation
import RxSwift
import RxCocoa
import CoreLocation


class P2PPresenter: ModulePresenter, P2PModule {
  weak var delegate: P2PModuleDelegate?
  var currentLocation = BehaviorRelay<CLLocation?>(value: nil)
  var trades = BehaviorRelay<Trades?>(value: nil)
  var accountStorage: AccountStorage?
  var walletUseCase: WalletUsecase?
  var userId: String?
  var locationService: LocationService?
  var mainSocketService: MainSocketService?
  var isCreationError = BehaviorRelay<Bool>(value: false)
  var tradeSuccessMessage = BehaviorRelay<String>(value: "")
  var balance = BehaviorRelay<CoinsBalance?>(value: nil)
  var createdOrder = BehaviorRelay<Order?>(value: nil)
  var updatedOrder = PublishRelay<Order>()
  var socketTrade = PublishRelay<Trade>()
  
  private var coins: [CoinBalance]?
    
  private let fetchDataRelay = PublishRelay<Void>()
  var errorService: ErrorService?
  
  func setup(trades: Trades, userId: String) {
    self.userId = userId

    self.trades.accept(trades)
    
    guard let wallet = walletUseCase else {
      return
    }
    
    fetchDataRelay
      .asObservable()
      .flatMap { [unowned self]  in
        return self.track(wallet.getCoinsBalance().asObservable())
      }.subscribe(onNext: { [weak self] result in
        self?.balance.accept(result)
        self?.coins = result.coins
      })
      .disposed(by: disposeBag)
    
    fetchDataRelay.accept(())
  }
  
  func subscribeOnSockets() {
    mainSocketService?.getTrade().subscribe(onNext: { [weak self] (trade) in
      self?.socketTrade.accept(trade)
    }).disposed(by: disposeBag)
    
    mainSocketService?.getOrder().subscribe(onNext: { [weak self] (order) in
      self?.updatedOrder.accept(order)
    }).disposed(by: disposeBag)
  }
  
  func didTap(type: OrderDetailsActionType, model: MyOrderViewModel) {
    if type == .cancel {
      guard let id = model.order.id else { return }
      walletUseCase?.cancelOrder(id: id).subscribeOn(MainScheduler()).subscribe(onSuccess: { [weak self] (order) in
        self?.updatedOrder.accept(order)
      }, onError: { error in
      }).disposed(by: disposeBag)
    } else {
      updateOrder(type: type, model: model)
    }
  }
  
  func updatedRate(model: MyOrderViewModel, rate: Int) {
    guard let id = model.order.id else { return }
    walletUseCase?.updateRate(id: id, rate: rate).subscribeOn(MainScheduler()).subscribe(onSuccess: { [weak self] (order) in
      self?.updatedOrder.accept(order)
    }, onError:{ error in
    }).disposed(by: disposeBag)
  }
  
  private func updateOrder(type: OrderDetailsActionType, model: MyOrderViewModel) {
    guard let id = model.order.id else { return }
    walletUseCase?.updateOrder(id: id, status: type, rate: nil).subscribeOn(MainScheduler()).subscribe(onSuccess: { [weak self] (order) in
      self?.updatedOrder.accept(order)
    }, onError:{ error in
    }).disposed(by: disposeBag)
  }
  
  func refreshTrades() {
    walletUseCase?.getTrades().subscribe(onSuccess: { [weak self] (trades) in
      self?.setup(trades: trades, userId: self?.userId ?? "")
    }, onError: { [weak self] (error) in
      guard let errorService = self?.errorService, let disposable = self?.disposeBag else {
        return
      }
      errorService.showError(for: .serverError).subscribe().disposed(by: disposable)
    }).disposed(by: disposeBag)
  }
    
    func openOrderDetails(order: Order) {
        walletUseCase?.getTrades().subscribeOn(MainScheduler()).subscribe(onSuccess: { [weak self] (trades) in
        self?.setup(trades: trades, userId: self?.userId ?? "")
        self?.createdOrder.accept(order)
      }, onError: { [weak self] (error) in
        guard let errorService = self?.errorService, let disposable = self?.disposeBag else {
          return
        }
        errorService.showError(for: .serverError).subscribe().disposed(by: disposable)
      }).disposed(by: disposeBag)
    }
  
  func checkLocation() {
    locationService?.requestLocationIfNeeded({ [weak self] (result) in
        self?.currentLocation.accept(result)
    })
  }
  
  func didSelectedSubmit(data: P2PCreateTradeDataModel) {
    guard let useCase = walletUseCase else { return }
  
    track(useCase.createTrade(data: data))
      .asObservable()
      .subscribe(onNext: { [weak self] (trade) in
        self?.tradeSuccessMessage.accept(localize(L.P2p.Trade.Created.message))
        self?.refreshTrades()
      }, onError: { [weak self] (error) in
          guard let errorService = self?.errorService, let disposable = self?.disposeBag else {
            return
          }
          errorService.showError(for: .serverError).subscribe().disposed(by: disposable)
      }).disposed(by: disposeBag)
  }
  
  func editTrade(data: P2PEditTradeDataModel) {
    guard let useCase = walletUseCase else { return }

    track(useCase.editTrade(data: data))
      .asObservable()
      .subscribe(onNext: { [weak self] (trade) in
        self?.tradeSuccessMessage.accept(localize(L.P2p.Trade.Updated.message))
        self?.refreshTrades()
      }, onError: { [weak self] (error) in
          guard let errorService = self?.errorService, let disposable = self?.disposeBag else {
            return
          }
          errorService.showError(for: .serverError).subscribe().disposed(by: disposable)
      }).disposed(by: disposeBag)
  }
    
    func cancelTrade(id: String) {
      guard let useCase = walletUseCase else { return }
    
      track(useCase.cancelTrade(id: id))
        .asObservable()
        .subscribe(onNext: { [weak self] (trade) in
          self?.tradeSuccessMessage.accept(localize(L.P2p.Trade.Created.message))
          self?.refreshTrades()
        }, onError: { [weak self] (error) in
            guard let errorService = self?.errorService, let disposable = self?.disposeBag else {
              return
            }
            errorService.showError(for: .serverError).subscribe().disposed(by: disposable)
        }).disposed(by: disposeBag)
    }
    
    func createOrder(model: P2PCreateOrderDataModel) {
        guard let useCase = walletUseCase else { return }
        track(useCase.createOrder(data: model)).asObservable().subscribe {[weak self] (order) in
            self?.openOrderDetails(order: order)
        } onError: {[weak self] (_) in
            guard let errorService = self?.errorService, let disposable = self?.disposeBag else {
              return
            }
            errorService.showError(for: .serverError).subscribe().disposed(by: disposable)
        }.disposed(by: disposeBag)
    }
}

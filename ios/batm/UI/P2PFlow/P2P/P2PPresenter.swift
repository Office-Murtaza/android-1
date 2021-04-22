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
  var userId: Int?
  
  var dismissTopController = BehaviorRelay<Bool>(value: false)
  
  var balance = PublishRelay<CoinsBalance>()
  private let locationService = GeolocationService()
  private let fetchDataRelay = PublishRelay<Void>()
  
  func setup(trades: Trades, userId: Int) {
    self.userId = userId
    self.trades.accept(trades)
    
    guard let wallet = walletUseCase else {
      return
    }
    
    fetchDataRelay
      .asObservable()
      .flatMap { [unowned self]  in
        return self.track(wallet.getCoinsBalance(filteredByActive: true).asObservable())
      }.subscribe(onNext: { [weak self] result in
        self?.balance.accept(result)
      })
      .disposed(by: disposeBag)
    
    fetchDataRelay.accept(())
    
  }
  
  func refreshTrades() {
    walletUseCase?.getTrades().subscribe(onSuccess: { [weak self] (trades) in
      self?.setup(trades: trades, userId: self?.userId ?? 0)
    }, onError: { (error) in
      print("error")
    })
  }
  
  func checkLocation() {
    locationService.requestLocation { [weak self] (result) in
      self?.currentLocation.accept(result)
    }
  }
  
  func didSelectedSubmit(data: P2PCreateTradeDataModel) {
    guard let useCase = walletUseCase else { return }
    //do we need track here?
    track(useCase.createTrade(data: data))
      .asObservable()
      .subscribe(onNext: { [weak self] (trade) in
        self?.dismissTopController.accept(true)
        self?.refreshTrades()
      }, onError: { (error) in
        print("error")
      }).disposed(by: disposeBag)
  }
}

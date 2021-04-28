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
  
  var isCreationError = BehaviorRelay<Bool>(value: false)
  var tradeSuccessMessage = BehaviorRelay<String>(value: "")
  
  var balance = PublishRelay<CoinsBalance>()
  private let locationService = GeolocationService()
  private let fetchDataRelay = PublishRelay<Void>()
  var errorService: ErrorService?
  
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
    }, onError: { [weak self] (error) in
      guard let errorService = self?.errorService, let disposable = self?.disposeBag else {
        return
      }
      errorService.showError(for: .serverError).subscribe().disposed(by: disposable)
    }).disposed(by: disposeBag)
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
        self?.tradeSuccessMessage.accept("Trade successfully created")
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
    //do we need track here?
    track(useCase.editTrade(data: data))
      .asObservable()
      .subscribe(onNext: { [weak self] (trade) in
        self?.tradeSuccessMessage.accept("Trade successfully updated")
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
      //do we need track here?
      track(useCase.cancelTrade(id: id))
        .asObservable()
        .subscribe(onNext: { [weak self] (trade) in
          self?.tradeSuccessMessage.accept("Trade successfully canceled")
          self?.refreshTrades()
        }, onError: { [weak self] (error) in
            guard let errorService = self?.errorService, let disposable = self?.disposeBag else {
              return
            }
            errorService.showError(for: .serverError).subscribe().disposed(by: disposable)
        }).disposed(by: disposeBag)
    }
}

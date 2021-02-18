import Foundation
import RxSwift
import RxCocoa

final class CoinSendGiftPresenter: ModulePresenter, CoinSendGiftModule {

  typealias Store = ViewStore<CoinSendGiftAction, CoinSendGiftState>

  struct Input {
    var updateCoinAmount: Driver<String?>
    var updateFromPickerItem: Driver<CustomCoinType>
    var maxFrom: Driver<Void>
    var updateMessage: Driver<String?>
    var updateImageId: Driver<String?>
    var submit: Driver<Void>
    var fromCoinType: Driver<CustomCoinType>
  }
  
  private let usecase: CoinDetailsUsecase
  private let store: Store
  private let walletUsecase: WalletUsecase
  private let balanceService: BalanceService
    
  weak var delegate: CoinSendGiftModuleDelegate?

  private let fetchDataRelay = PublishRelay<Void>()
    
  var state: Driver<CoinSendGiftState> {
    return store.state
  }
  
  init(usecase: CoinDetailsUsecase,
       store: Store = CoinSendGiftStore(),
       walletUseCase: WalletUsecase,
       balanceService: BalanceService) {
    self.usecase = usecase
    self.store = store
    self.walletUsecase = walletUseCase
    self.balanceService = balanceService
  }
  
  func setup(coin: BTMCoin, coinBalances: [CoinBalance], coinDetails: CoinDetails) {
    store.action.accept(.setupCoin(coin))
    store.action.accept(.setupCoinBalances(coinBalances))
    store.action.accept(.setupCoinDetails(coinDetails))
  }

  func setupContact(_ contact: BContact) {
    store.action.accept(.setupContact(contact))
    store.action.accept(.updatePhone(contact.phones.first))
  }

  func bind(input: Input) {
    
    
    fetchDataRelay
        .asObservable()
      .flatMap { [unowned self]  in
        return self.track(Observable.combineLatest(self.balanceService.getCoinsBalance().asObservable(),
                                                   self.walletUsecase.getCoinDetails(for: CustomCoinType.bitcoin).asObservable(),
                                                   self.walletUsecase.getCoinsList().asObservable()))
      }.subscribe({ [weak self] in
        guard let coinBalance = $0.element?.0, let coinDetails = $0.element?.1, let coins = $0.element?.2 else { return }
        self?.store.action.accept(.finishFetchingCoinsData(coinBalance, coinDetails, coins))
      })
      .disposed(by: disposeBag)
    
    
    input.fromCoinType
              .asObservable()
              .distinctUntilChanged()
              .observeOn(MainScheduler.instance)
              .flatMap { [unowned self] type in self.track(self.usecase.getCoinDetails(for: type))}
              .subscribe { [unowned self] result in
                switch result {
                case let .next(details):
                  self.store.action.accept(.setupCoinDetails(details))
                default: break
                }
              }.disposed(by: disposeBag)
    
    
    
    input.updateFromPickerItem
             .asObservable()
           .observeOn(MainScheduler.instance)
             .flatMap { [unowned self] type in
               self.track(self.usecase.getCoin(for: type))
             }
             .subscribe { [unowned self] result in
               switch result {
               case let .next(coin):
                   self.store.action.accept(.updateFromCoin(coin))
                   self.store.action.accept(.updateFromCoinType(coin.type))
               default: break
               }
             }.disposed(by: disposeBag)
    
    
    input.maxFrom
        .asObservable()
        .withLatestFrom(state)
        .map { $0.maxFromValue.coinFormatted }
        .map { CoinSendGiftAction.updateCoinAmount($0) }
        .bind(to: store.action)
        .disposed(by: disposeBag)
    
    
    input.updateCoinAmount
      .asObservable()
      .map { CoinSendGiftAction.updateCoinAmount($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateMessage
      .asObservable()
      .map { CoinSendGiftAction.updateMessage($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.updateImageId
      .asObservable()
      .map { CoinSendGiftAction.updateImageId($0) }
      .bind(to: store.action)
      .disposed(by: disposeBag)
    
    input.submit
      .asObservable()
      .doOnNext { [store] in store.action.accept(.updateValidationState) }
      .withLatestFrom(state)
      .filter { $0.validationState.isValid }
      .flatMap { [unowned self] in self.track(self.sendGift(for: $0)) }
      .subscribe(onNext: { [delegate] in
                  delegate?.didFinishCoinSendGift()
      })
      .disposed(by: disposeBag)
    
    fetchDataRelay.accept(())
  }
  
  private func sendGift(for state: CoinSendGiftState) -> Completable {
    return usecase.sendGift(from: state.fromCoin!,
                                with: state.coinDetails!,
                                to: state.phoneE164,
                                amount: state.coinAmount.decimalValue ?? 0.0,
                                message: state.message,
                                imageId: state.imageId)
      .catchError { [store] in
        if let apiError = $0 as? APIError, case let .serverError(error) = apiError, let code = error.code, code > 1 {
          store.action.accept(.updatePhoneError(error.message))
        }
        
        throw $0
      }
  }
}

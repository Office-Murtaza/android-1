import Foundation
import RxSwift
import RxCocoa

final class CoinDepositPresenter: ModulePresenter, CoinDepositModule {
    struct Input {
        var copy: Driver<Void>
    }
    
    weak var delegate: CoinDepositModuleDelegate?
    let didViewLoadRelay = PublishRelay<Void>()
    let didCoinLoadRelay = PublishRelay<BTMCoin?>()
    
    private var coin: BTMCoin?
    private var coinType: CustomCoinType?
    private let usecase: CoinDetailsUsecase
    
    init(usecase: CoinDetailsUsecase) {
        self.usecase = usecase
    }
    
    func setup(with coinType: CustomCoinType) {
        self.coinType = coinType
    }
    
    func bind(input: Input) {
        didViewLoadRelay
            .flatMap { [unowned self] _ in
                return self.track(self.usecase.getCoin(for: self.coinType ?? .bitcoin)
                                    .do(onSuccess: { [weak self] coin in
                                        self?.coin = coin
                                        self?.didCoinLoadRelay.accept(coin)
                                    }).asObservable())
            }
            .subscribe()
            .disposed(by: disposeBag)
        
        input.copy
            .drive(onNext: { [unowned self] in UIPasteboard.general.string = self.coin?.address })
            .disposed(by: disposeBag)
    }
}

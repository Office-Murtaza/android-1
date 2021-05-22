import Foundation
import Swinject

class P2PAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<P2PModule>.self) { resolver in
            let viewController = P2PViewController()
            let presenter = P2PPresenter()
            presenter.accountStorage = resolver.resolve(AccountStorage.self)
            presenter.delegate = resolver.resolve(P2PModuleDelegate.self)
            presenter.walletUseCase = resolver.resolve(WalletUsecase.self)
            presenter.errorService = resolver.resolve(ErrorService.self)
            presenter.tradeSocketService = resolver.resolve(TradeSocketService.self)
            presenter.locationService = resolver.resolve(LocationService.self)
            viewController.presenter = presenter
            viewController.buyViewController = TradeListViewController(type: .buy)
            viewController.sellViewController = TradeListViewController(type: .sell)
            viewController.myViewController = MyViewController()
            
            return Module<P2PModule>(controller: viewController, input: presenter)
        }
    }
}

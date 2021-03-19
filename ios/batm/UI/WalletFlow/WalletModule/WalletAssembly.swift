import Foundation
import Swinject

class WalletAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<WalletModule>.self) { resolver in
            let dataSource = WalletTableViewDataSource()
            let viewController = WalletViewController()
            let usecase = resolver.resolve(WalletUsecase.self)!
            let presenter = WalletPresenter(usecase: usecase)
            
            presenter.delegate = resolver.resolve(WalletModuleDelegate.self)
            viewController.presenter = presenter
            viewController.dataSource = dataSource
            
            return Module<WalletModule>(controller: viewController, input: presenter)
        }
    }
}

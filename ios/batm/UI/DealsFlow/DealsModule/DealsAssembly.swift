import Foundation
import Swinject

class DealsAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<DealsModule>.self) { resolver in
            let dataSource = DealsTableViewDataSource()
            let viewController = DealsViewController()
            let usecase = resolver.resolve(DealsUsecase.self)!
            let balanceService = resolver.resolve(BalanceService.self)!
            let presenter = DealsPresenter(usecase: usecase, balanceService: balanceService)
            
            presenter.delegate = resolver.resolve(DealsModuleDelegate.self)
            viewController.presenter = presenter
            viewController.dataSource = dataSource
            
            return Module<DealsModule>(controller: viewController, input: presenter)
        }
    }
}

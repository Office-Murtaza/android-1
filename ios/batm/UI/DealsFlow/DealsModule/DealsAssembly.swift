import Foundation
import Swinject

class DealsAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<DealsModule>.self) { resolver in
            let viewController = DealsViewController()
            let usecase = resolver.resolve(DealsUsecase.self)!
            let presenter = DealsPresenter(usecase: usecase)
            
            presenter.delegate = resolver.resolve(DealsModuleDelegate.self)
            viewController.presenter = presenter
            
            return Module<DealsModule>(controller: viewController, input: presenter)
        }
    }
}

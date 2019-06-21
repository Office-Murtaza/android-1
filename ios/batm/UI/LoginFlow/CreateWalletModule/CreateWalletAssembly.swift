import Foundation
import Swinject

class CreateWalletAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CreateWalletModule>.self) { resolver in
      let viewController = CreateWalletViewController()
      let usecase = resolver.resolve(LoginUsecase.self)!
      let presenter = CreateWalletPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(CreateWalletModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CreateWalletModule>(controller: viewController, input: presenter)
    }
  }
}

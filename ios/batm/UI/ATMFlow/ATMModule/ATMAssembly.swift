import Foundation
import Swinject

class ATMAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<ATMModule>.self) { resolver in
      let viewController = ATMViewController()
      let usecase = resolver.resolve(ATMUsecase.self)!
      let presenter = ATMPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(ATMModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<ATMModule>(controller: viewController, input: presenter)
    }
  }
}

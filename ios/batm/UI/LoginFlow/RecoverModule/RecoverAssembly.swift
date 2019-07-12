import Foundation
import Swinject

class RecoverAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<RecoverModule>.self) { resolver in
      let viewController = RecoverViewController()
      let usecase = resolver.resolve(LoginUsecase.self)!
      let presenter = RecoverPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(RecoverModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<RecoverModule>(controller: viewController, input: presenter)
    }
  }
}

import Foundation
import Swinject

class PinCodeAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<PinCodeModule>.self) { resolver in
      let viewController = PinCodeViewController()
      let usecase = resolver.resolve(PinCodeUsecase.self)!
      let presenter = PinCodePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(PinCodeModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<PinCodeModule>(controller: viewController, input: presenter)
    }
  }
}

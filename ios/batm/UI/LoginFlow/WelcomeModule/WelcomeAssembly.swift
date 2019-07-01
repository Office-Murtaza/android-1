import Foundation
import Swinject

class WelcomeAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<WelcomeModule>.self) { resolver in
      let viewController = WelcomeViewController()
      let presenter = WelcomePresenter()
      
      presenter.delegate = resolver.resolve(WelcomeModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<WelcomeModule>(controller: viewController, input: presenter)
    }
  }
}

import Foundation
import Swinject

class ShowPhoneAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<ShowPhoneModule>.self) { resolver in
      let viewController = ShowPhoneViewController()
      let presenter = ShowPhonePresenter()
      
      presenter.delegate = resolver.resolve(ShowPhoneModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<ShowPhoneModule>(controller: viewController, input: presenter)
    }
  }
}

import Foundation
import Swinject
import UIKit

final class EnterPasswordAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<EnterPasswordModule>.self) { resolver in
      let viewController = EnterPasswordViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = EnterPasswordPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(EnterPasswordModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<EnterPasswordModule>(controller: viewController, input: presenter)
    }
  }
}

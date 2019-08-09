import Foundation
import Swinject
import UIKit

final class ChangePasswordAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<ChangePasswordModule>.self) { resolver in
      let viewController = ChangePasswordViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = ChangePasswordPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(ChangePasswordModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<ChangePasswordModule>(controller: viewController, input: presenter)
    }
  }
}

import Foundation
import Swinject
import UIKit

final class ChangePinAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<ChangePinModule>.self) { resolver in
      let viewController = ChangePinViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = ChangePinPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(ChangePinModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<ChangePinModule>(controller: viewController, input: presenter)
    }
  }
}

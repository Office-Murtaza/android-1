import Foundation
import Swinject
import UIKit

final class UpdatePasswordAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<UpdatePasswordModule>.self) { resolver in
      let viewController = UpdatePasswordViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = UpdatePasswordPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(UpdatePasswordModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<UpdatePasswordModule>(controller: viewController, input: presenter)
    }
  }
}

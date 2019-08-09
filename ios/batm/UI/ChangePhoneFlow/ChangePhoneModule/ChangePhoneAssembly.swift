import Foundation
import Swinject
import UIKit

final class ChangePhoneAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<ChangePhoneModule>.self) { resolver in
      let viewController = ChangePhoneViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = ChangePhonePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(ChangePhoneModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<ChangePhoneModule>(controller: viewController, input: presenter)
    }
  }
}

import Foundation
import Swinject
import UIKit

final class UpdatePhoneAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<UpdatePhoneModule>.self) { resolver in
      let viewController = UpdatePhoneViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = UpdatePhonePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(UpdatePhoneModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<UpdatePhoneModule>(controller: viewController, input: presenter)
    }
  }
}

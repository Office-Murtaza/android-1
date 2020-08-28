import Foundation
import Swinject
import UIKit

final class VerificationAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<VerificationModule>.self) { resolver in
      let viewController = VerificationViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = VerificationPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(VerificationModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<VerificationModule>(controller: viewController, input: presenter)
    }
  }
}

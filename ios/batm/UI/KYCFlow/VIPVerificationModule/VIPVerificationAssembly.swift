import Foundation
import Swinject
import UIKit

final class VIPVerificationAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<VIPVerificationModule>.self) { resolver in
      let viewController = VIPVerificationViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = VIPVerificationPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(VIPVerificationModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<VIPVerificationModule>(controller: viewController, input: presenter)
    }
  }
}

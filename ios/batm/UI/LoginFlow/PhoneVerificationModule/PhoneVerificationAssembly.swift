import Foundation
import Swinject
import UIKit

final class PhoneVerificationAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<PhoneVerificationModule>.self) { resolver in
      let viewController = PhoneVerificationViewController()
      let usecase = resolver.resolve(LoginUsecase.self)!
      let presenter = PhoneVerificationPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(PhoneVerificationModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<PhoneVerificationModule>(controller: viewController, input: presenter)
    }
  }
}

import Foundation
import Swinject
import UIKit

final class VerificationInfoAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<VerificationInfoModule>.self) { resolver in
      let viewController = VerificationInfoViewController()
      let presenter = VerificationInfoPresenter()
      
      presenter.delegate = resolver.resolve(VerificationInfoModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<VerificationInfoModule>(controller: viewController, input: presenter)
    }
  }
}

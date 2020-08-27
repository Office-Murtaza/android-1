import Foundation
import Swinject
import UIKit

final class KYCAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<KYCModule>.self) { resolver in
      let viewController = KYCViewController()
      let presenter = KYCPresenter()
      
      presenter.delegate = resolver.resolve(KYCModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<KYCModule>(controller: viewController, input: presenter)
    }
  }
}

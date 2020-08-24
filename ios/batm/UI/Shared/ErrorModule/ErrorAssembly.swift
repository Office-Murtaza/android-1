import Foundation
import Swinject
import UIKit

final class ErrorAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<ErrorModule>.self) { resolver in
      let viewController = ErrorViewController()
      let presenter = ErrorPresenter()
      
      presenter.delegate = resolver.resolve(ErrorModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<ErrorModule>(controller: viewController, input: presenter)
    }
  }
}

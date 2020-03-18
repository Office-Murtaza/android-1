import Foundation
import Swinject
import UIKit

final class ___FILEBASENAME___: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<___VARIABLE_moduleName___Module>.self) { resolver in
      let viewController = ___VARIABLE_moduleName___ViewController()
      let presenter = ___VARIABLE_moduleName___Presenter()
      
      presenter.delegate = resolver.resolve(___VARIABLE_moduleName___ModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<___VARIABLE_moduleName___Module>(controller: viewController, input: presenter)
    }
  }
}

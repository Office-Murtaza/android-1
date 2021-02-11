import Foundation
import Swinject
import UIKit

final class SecurityAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<SecurityModule>.self) { resolver in
      let dataSource = SecurityTableViewDataSource()
      let viewController = SecurityViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = SecurityPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(SecurityModuleDelegate.self)
      viewController.presenter = presenter
      viewController.dataSource = dataSource
      
      return Module<SecurityModule>(controller: viewController, input: presenter)
    }
  }
}

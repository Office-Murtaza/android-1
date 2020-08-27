import Foundation
import Swinject
import UIKit

final class AboutAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<AboutModule>.self) { resolver in
      let dataSource = SettingsTableViewDataSource()
      let viewController = AboutViewController()
      let presenter = AboutPresenter()
      
      presenter.delegate = resolver.resolve(AboutModuleDelegate.self)
      viewController.presenter = presenter
      viewController.dataSource = dataSource
      
      return Module<AboutModule>(controller: viewController, input: presenter)
    }
  }
}

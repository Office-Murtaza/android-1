import Foundation
import Swinject

class SettingsAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<SettingsModule>.self) { resolver in
      let dataSource = SettingsTableViewDataSource()
      let viewController = SettingsViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = SettingsPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(SettingsModuleDelegate.self)
      viewController.presenter = presenter
      viewController.dataSource = dataSource
      
      return Module<SettingsModule>(controller: viewController, input: presenter)
    }
  }
}

import Foundation
import Swinject

class SettingsAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<SettingsModule>.self) { resolver in
      let viewController = SettingsViewController()
      let presenter = SettingsPresenter()
      
      presenter.delegate = resolver.resolve(SettingsModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<SettingsModule>(controller: viewController, input: presenter)
    }
  }
}

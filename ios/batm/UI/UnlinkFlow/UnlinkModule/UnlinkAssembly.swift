import Foundation
import Swinject
import UIKit

final class UnlinkAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<UnlinkModule>.self) { resolver in
      let viewController = UnlinkViewController()
      let usecase = resolver.resolve(SettingsUsecase.self)!
      let presenter = UnlinkPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(UnlinkModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<UnlinkModule>(controller: viewController, input: presenter)
    }
  }
}

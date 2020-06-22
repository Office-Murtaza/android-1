import Foundation
import Swinject
import UIKit

final class CreateEditTradeAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CreateEditTradeModule>.self) { resolver in
      let viewController = CreateEditTradeViewController()
      let usecase = resolver.resolve(TradesUsecase.self)!
      let presenter = CreateEditTradePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(CreateEditTradeModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CreateEditTradeModule>(controller: viewController, input: presenter)
    }
  }
}

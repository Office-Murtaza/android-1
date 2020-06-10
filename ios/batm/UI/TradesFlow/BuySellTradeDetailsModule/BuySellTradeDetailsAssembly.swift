import Foundation
import Swinject
import UIKit

final class BuySellTradeDetailsAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<BuySellTradeDetailsModule>.self) { resolver in
      let viewController = BuySellTradeDetailsViewController()
      let usecase = resolver.resolve(TradesUsecase.self)!
      let presenter = BuySellTradeDetailsPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(BuySellTradeDetailsModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<BuySellTradeDetailsModule>(controller: viewController, input: presenter)
    }
  }
}

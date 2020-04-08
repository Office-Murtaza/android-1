import Foundation
import Swinject
import UIKit

final class CoinExchangeAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinExchangeModule>.self) { resolver in
      let viewController = CoinExchangeViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let presenter = CoinExchangePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(CoinExchangeModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinExchangeModule>(controller: viewController, input: presenter)
    }
  }
}

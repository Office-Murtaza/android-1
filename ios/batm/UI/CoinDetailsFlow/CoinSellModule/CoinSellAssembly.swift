import Foundation
import Swinject
import UIKit

final class CoinSellAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinSellModule>.self) { resolver in
      let viewController = CoinSellViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let presenter = CoinSellPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(CoinSellModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinSellModule>(controller: viewController, input: presenter)
    }
  }
}

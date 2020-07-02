import Foundation
import Swinject
import UIKit

final class CoinStakingAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinStakingModule>.self) { resolver in
      let viewController = CoinStakingViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let presenter = CoinStakingPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(CoinStakingModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinStakingModule>(controller: viewController, input: presenter)
    }
  }
}

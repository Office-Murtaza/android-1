import Foundation
import Swinject
import UIKit

final class CoinWithdrawAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinWithdrawModule>.self) { resolver in
      let viewController = CoinWithdrawViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let presenter = CoinWithdrawPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(CoinWithdrawModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinWithdrawModule>(controller: viewController, input: presenter)
    }
  }
}

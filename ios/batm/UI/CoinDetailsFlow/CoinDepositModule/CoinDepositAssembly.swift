import Foundation
import Swinject
import UIKit

final class CoinDepositAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinDepositModule>.self) { resolver in
      let viewController = CoinDepositViewController()
      let presenter = CoinDepositPresenter()
      
      presenter.delegate = resolver.resolve(CoinDepositModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinDepositModule>(controller: viewController, input: presenter)
    }
  }
}

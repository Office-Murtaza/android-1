import Foundation
import Swinject
import UIKit

final class CoinSellDetailsAnotherAddressAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinSellDetailsAnotherAddressModule>.self) { resolver in
      let viewController = CoinSellDetailsAnotherAddressViewController()
      let presenter = CoinSellDetailsAnotherAddressPresenter()
      
      presenter.delegate = resolver.resolve(CoinSellDetailsAnotherAddressModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinSellDetailsAnotherAddressModule>(controller: viewController, input: presenter)
    }
  }
}

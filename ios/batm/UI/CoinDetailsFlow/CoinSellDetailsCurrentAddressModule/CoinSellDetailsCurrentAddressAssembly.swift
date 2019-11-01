import Foundation
import Swinject
import UIKit

final class CoinSellDetailsCurrentAddressAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinSellDetailsCurrentAddressModule>.self) { resolver in
      let viewController = CoinSellDetailsCurrentAddressViewController()
      let presenter = CoinSellDetailsCurrentAddressPresenter()
      
      presenter.delegate = resolver.resolve(CoinSellDetailsCurrentAddressModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinSellDetailsCurrentAddressModule>(controller: viewController, input: presenter)
    }
  }
}

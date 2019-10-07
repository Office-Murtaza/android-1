import Foundation
import Swinject
import UIKit

final class CoinSendGiftAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinSendGiftModule>.self) { resolver in
      let viewController = CoinSendGiftViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let presenter = CoinSendGiftPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(CoinSendGiftModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinSendGiftModule>(controller: viewController, input: presenter)
    }
  }
}

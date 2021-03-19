import Foundation
import Swinject
import UIKit

final class ReserveAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<ReserveModule>.self) { resolver in
      let viewController = ReserveViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let presenter = ReservePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(ReserveModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<ReserveModule>(controller: viewController, input: presenter)
    }
  }
}

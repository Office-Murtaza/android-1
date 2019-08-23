import Foundation
import Swinject
import UIKit

final class CoinDetailsAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinDetailsModule>.self) { resolver in
      let viewController = CoinDetailsViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let presenter = CoinDetailsPresenter(usecase: usecase)
      
      let dataSource = CoinDetailsCollectionViewDataSource()
      viewController.dataSource = dataSource
      
      presenter.delegate = resolver.resolve(CoinDetailsModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinDetailsModule>(controller: viewController, input: presenter)
    }
  }
}

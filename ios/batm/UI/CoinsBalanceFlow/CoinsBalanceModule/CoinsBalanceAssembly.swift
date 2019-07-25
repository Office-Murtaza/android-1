import Foundation
import Swinject

class CoinsBalanceAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinsBalanceModule>.self) { resolver in
      let dataSource = CoinsBalanceCollectionViewDataSource()
      let viewController = CoinsBalanceViewController()
      let usecase = resolver.resolve(CoinsBalanceUsecase.self)!
      let presenter = CoinsBalancePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(CoinsBalanceModuleDelegate.self)
      viewController.presenter = presenter
      viewController.dataSource = dataSource
      
      return Module<CoinsBalanceModule>(controller: viewController, input: presenter)
    }
  }
}

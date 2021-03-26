import Foundation
import Swinject
import UIKit

final class CoinDetailsAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinDetailsModule>.self) { resolver in
      let viewController = CoinDetailsViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let walletUsecase = resolver.resolve(WalletUsecase.self)!
      let balanceService = resolver.resolve(BalanceService.self)!
      let presenter = CoinDetailsPresenter(usecase: usecase,
                                           walletUsecase: walletUsecase,
                                           balanceService: balanceService)
      
      let dataSource = CoinDetailsTableViewDataSource()
      viewController.dataSource = dataSource
      
      presenter.delegate = resolver.resolve(CoinDetailsModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<CoinDetailsModule>(controller: viewController, input: presenter)
    }
  }
}

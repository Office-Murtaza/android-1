import Foundation
import Swinject
import UIKit

final class CoinSendGiftAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<CoinSendGiftModule>.self) { resolver in
      let viewController = CoinSendGiftViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let walletUseCase = resolver.resolve(WalletUsecase.self)!
      let balanceService = resolver.resolve(BalanceService.self)!
      let presenter = CoinSendGiftPresenter(usecase: usecase, walletUseCase: walletUseCase, balanceService: balanceService)
      
      presenter.delegate = resolver.resolve(DealsFlowController.self)
      viewController.presenter = presenter
      
      return Module<CoinSendGiftModule>(controller: viewController, input: presenter)
    }
  }
}

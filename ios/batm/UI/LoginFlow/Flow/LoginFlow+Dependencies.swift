import Foundation
import Swinject

extension LoginFlow {
  
  class Dependencies: Assembly {
    
    func assemble(container: Container) {
      container
        .register(LoginFlowController.self) { ioc in
          let loginController = LoginFlowController()
          loginController.delegate = ioc.resolve(LoginFlowDelegate.self)
          return loginController
        }
        .inObjectScope(.container)
        .implements(WelcomeModuleDelegate.self,
                    CreateWalletModuleDelegate.self,
                    SeedPhraseModuleDelegate.self)
      
      assembleUsecases(container: container)
    }
    
    func assembleUsecases(container: Container) {
      container.register(LoginUsecase.self) { ioc in
        let api = ioc.resolve(APIGateway.self)!
        let accountStorage = ioc.resolve(AccountStorage.self)!
        let walletStorage = ioc.resolve(BTMWalletStorage.self)!
        let walletService = ioc.resolve(WalletService.self)!
        return LoginUsecaseImpl(api: api,
                                accountStorage: accountStorage,
                                walletStorage: walletStorage,
                                walletService: walletService)
        }.inObjectScope(.container)
    }
  }
}

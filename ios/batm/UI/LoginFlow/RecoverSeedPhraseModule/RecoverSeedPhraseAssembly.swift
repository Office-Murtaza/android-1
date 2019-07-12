import Foundation
import Swinject

class RecoverSeedPhraseAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<RecoverSeedPhraseModule>.self) { resolver in
      let viewController = RecoverSeedPhraseViewController()
      let usecase = resolver.resolve(LoginUsecase.self)!
      let presenter = RecoverSeedPhrasePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(RecoverSeedPhraseModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<RecoverSeedPhraseModule>(controller: viewController, input: presenter)
    }
  }
}

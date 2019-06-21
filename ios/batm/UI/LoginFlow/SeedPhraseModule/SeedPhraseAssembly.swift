import Foundation
import Swinject

class SeedPhraseAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<SeedPhraseModule>.self) { resolver in
      let viewController = SeedPhraseViewController()
      let usecase = resolver.resolve(LoginUsecase.self)!
      let presenter = SeedPhrasePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(SeedPhraseModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<SeedPhraseModule>(controller: viewController, input: presenter)
    }
  }
}

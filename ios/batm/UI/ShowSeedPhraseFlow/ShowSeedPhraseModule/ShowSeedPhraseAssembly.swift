import Foundation
import Swinject
import UIKit

final class ShowSeedPhraseAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<ShowSeedPhraseModule>.self) { resolver in
      let viewController = ShowSeedPhraseViewController()
      let usecase = resolver.resolve(LoginUsecase.self)!
      let presenter = ShowSeedPhrasePresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(ShowSeedPhraseModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<ShowSeedPhraseModule>(controller: viewController, input: presenter)
    }
  }
}

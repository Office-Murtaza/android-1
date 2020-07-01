import Foundation
import Swinject
import UIKit

final class RecallAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<RecallModule>.self) { resolver in
      let viewController = RecallViewController()
      let usecase = resolver.resolve(CoinDetailsUsecase.self)!
      let presenter = RecallPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(RecallModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<RecallModule>(controller: viewController, input: presenter)
    }
  }
}

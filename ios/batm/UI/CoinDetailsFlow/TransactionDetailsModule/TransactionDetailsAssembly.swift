import Foundation
import Swinject
import UIKit

final class TransactionDetailsAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<TransactionDetailsModule>.self) { resolver in
      let viewController = TransactionDetailsViewController()
      let presenter = TransactionDetailsPresenter()
      
      presenter.delegate = resolver.resolve(TransactionDetailsModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<TransactionDetailsModule>(controller: viewController, input: presenter)
    }
  }
}

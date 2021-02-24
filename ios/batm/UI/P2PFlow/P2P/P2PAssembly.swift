import Foundation
import Swinject

class P2PAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<P2PModule>.self) { resolver in
          let viewController = P2PViewController()
          let presenter = P2PPresenter()
          
          presenter.delegate = resolver.resolve(P2PModuleDelegate.self)
          viewController.presenter = presenter
            viewController.buyViewController = TradeListViewController(color: .green)
            viewController.sellViewController = TradeListViewController(color: .orange)
            viewController.myViewController = TradeListViewController(color: .gray)
            
            
          return Module<P2PModule>(controller: viewController, input: presenter)
        }
    }
}

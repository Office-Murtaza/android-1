import Foundation
import Swinject
import UIKit

final class TransactionDetailsAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<TransactionDetailsModule>.self) { resolver in
            let dataSource = TransactionDetailsDataSource()
            let viewController = TransactionDetailsViewController()
            let usecase = resolver.resolve(CoinDetailsUsecase.self)!
            let presenter = TransactionDetailsPresenter(usecase: usecase)
            
            presenter.delegate = resolver.resolve(TransactionDetailsModuleDelegate.self)
            viewController.presenter = presenter
            viewController.dataSource = dataSource            
            
            return Module<TransactionDetailsModule>(controller: viewController, input: presenter)
        }
    }
}

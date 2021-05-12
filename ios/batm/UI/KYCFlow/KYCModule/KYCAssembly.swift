import Foundation
import Swinject
import UIKit

final class KYCAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<KYCModule>.self) { resolver in
            let usecase = resolver.resolve(SettingsUsecase.self)!
            let viewController = KYCViewController()
            let presenter = KYCPresenter(usecase: usecase)
            
            let dataSource = KYCDataSource()
            viewController.dataSource = dataSource
            
            presenter.delegate = resolver.resolve(KYCModuleDelegate.self)
            viewController.presenter = presenter
            
            return Module<KYCModule>(controller: viewController, input: presenter)
        }
    }
}

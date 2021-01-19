import Foundation
import UIKit

final class TransferSelectReceiverAssembly: Assembly {
    func assemble(container: Container) {
        container.register(Module<TransferModule>.self) { resolver in
            let viewController = TransferSelectReceiverViewController()
            let presenter = TransferSelectReceiverPresenter()
            presenter.delegate = resolver.resolve(TransferModuleDelegate.self)
            viewController.presenter = presenter
            return Module<TransferModule>(controller: viewController, input: presenter)
        }
    }
}

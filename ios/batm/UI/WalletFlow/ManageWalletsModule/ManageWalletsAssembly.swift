import Foundation
import Swinject

class ManageWalletsAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<ManageWalletsModule>.self) { resolver in
      let dataSource = ManageWalletsTableViewDataSource()
      let viewController = ManageWalletsViewController()
      let usecase = resolver.resolve(ManageWalletsUsecase.self)!
      let presenter = ManageWalletsPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(ManageWalletsModuleDelegate.self)
      viewController.presenter = presenter
      viewController.dataSource = dataSource
      
      return Module<ManageWalletsModule>(controller: viewController, input: presenter)
    }
  }
}

import Foundation
import Swinject

class FIlterCoinsAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<FilterCoinsModule>.self) { resolver in
      let dataSource = FilterCoinsCollectionViewDataSource()
      let viewController = FilterCoinsViewController()
      let usecase = resolver.resolve(FilterCoinsUsecase.self)!
      let presenter = FilterCoinsPresenter(usecase: usecase)
      
      presenter.delegate = resolver.resolve(FilterCoinsModuleDelegate.self)
      viewController.presenter = presenter
      viewController.dataSource = dataSource
      
      return Module<FilterCoinsModule>(controller: viewController, input: presenter)
    }
  }
}

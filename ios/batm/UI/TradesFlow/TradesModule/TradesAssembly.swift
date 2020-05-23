import Foundation
import Swinject
import UIKit

final class TradesAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<TradesModule>.self) { resolver in
      let buyTradesDataSource = BuySellTradesTableViewDataSource()
      let sellTradesDataSource = BuySellTradesTableViewDataSource()
      let viewController = TradesViewController()
      viewController.buyTradesDataSource = buyTradesDataSource
      viewController.sellTradesDataSource = sellTradesDataSource
      
      let usecase = resolver.resolve(TradesUsecase.self)!
      let locationService = resolver.resolve(LocationService.self)!
      let presenter = TradesPresenter(usecase: usecase, locationService: locationService)
      presenter.delegate = resolver.resolve(TradesModuleDelegate.self)
      viewController.presenter = presenter
      
      return Module<TradesModule>(controller: viewController, input: presenter)
    }
  }
}

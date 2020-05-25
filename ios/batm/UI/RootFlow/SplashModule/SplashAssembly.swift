import Foundation
import Swinject
import UIKit

final class SplashAssembly: Assembly {
  
  func assemble(container: Container) {
    container.register(Module<SplashModule>.self) { resolver in
      let viewController = SplashViewController()
      return Module<SplashModule>(controller: viewController, input: viewController)
    }
  }
}

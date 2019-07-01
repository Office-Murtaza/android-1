import Foundation
import Swinject

class DIInitializer: Initializer {
  
  func initialize(with options: LaunchOptions?, assembler: Assembler, container: Container) {
    assembler.apply(assemblies: assemblies)
  }
  
  private var assemblies: [Assembly] {
    return [
      RootFlow.Dependencies(),
      AppAssembly(),
      PersistentStackAssembly()
    ]
  }
}

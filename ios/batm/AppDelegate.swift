import UIKit
import RxFlow

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
  
  let container = Container()
  let assembler: Assembler
  let coordinator = Coordinator()
  var rootFlow: RootFlow!
  
  var window: UIWindow?
  
  override init() {
    assembler = Assembler(container: container)
    super.init()
  }
  
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    guard let window = self.window else { return false }
    
    initializers.forEach { $0.initialize(with: launchOptions,
                                         assembler: assembler,
                                         container: container) }
    
    rootFlow = RootFlow(view: window, parent: assembler)
    coordinator.coordinate(flow: rootFlow, withStepper: rootFlow.stepper)
    
    return true
  }
  
  private var initializers: [Initializer] {
    return [
      DIInitializer(),
      GoogleServicesInitializer(),
      GiphyInitializer(),
      ToastInitializer(),
    ]
  }
}


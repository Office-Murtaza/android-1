import UIKit
import RxFlow
import FirebaseMessaging
import Contacts

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
  let container = Container()
  let assembler: Assembler
  let coordinator = Coordinator()
  var rootFlow: RootFlow!
  
  var window: UIWindow?
  
  private var initializers: [Initializer] {
    return [
        DIInitializer(),
        FirebaseInitializer(),
        GoogleServicesInitializer(),
        GiphyInitializer(),
        ToastInitializer(),
    ]
  }
    
  override init() {
    assembler = Assembler(container: container)
    super.init()
  }
  
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    guard let window = self.window else { return false }
    
    CNContactStore().requestAccess(for: .contacts, completionHandler: { (result, error) in })
    
    initializers.forEach { $0.initialize(with: launchOptions,
                                         assembler: assembler,
                                         container: container) }
    
    rootFlow = RootFlow(view: window, parent: assembler)
    coordinator.coordinate(flow: rootFlow, withStepper: rootFlow.stepper)
    registerRemoteNotifications(with: application)
    
    return true
  }
}

extension AppDelegate: UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                  willPresent notification: UNNotification,
                                  withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.alert, .sound])
      }

    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String) {
        KeychainManager.save(value: fcmToken, for: GlobalConstants.fcmPushToken)
    }
    
    private func registerRemoteNotifications(with application: UIApplication) {
        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self
        
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions, completionHandler: { (registered, error) in
            if registered {
                DispatchQueue.main.async {
                    UserDefaultsHelper.notificationsEnabled = true
                    application.registerForRemoteNotifications()
                }
            }
        })
    }
}

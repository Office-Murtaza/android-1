import UIKit

extension UIApplication {
    var appVersion: String {
        guard let dictionary = Bundle.main.infoDictionary,
              let version = dictionary["CFBundleShortVersionString"] as? String
        else { return "" }
        return version
    }
}

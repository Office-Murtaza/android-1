import Foundation

class UserDefaultsHelper {
    enum Consts: String {
        case notificationsEnabled
    }
    
    static let defs = UserDefaults.standard
    
    @UserDefaultOptional(key: .notificationsEnabled)
    static var notificationsEnabled: Bool?
    
    static func reset() {
        let dict = defs.dictionaryRepresentation()
        dict.forEach { (key, _) in
            defs.removeObject(forKey: key)
        }
        defs.synchronize()
    }
}

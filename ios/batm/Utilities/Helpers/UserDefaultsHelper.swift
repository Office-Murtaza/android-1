import Foundation

class UserDefaultsHelper {
    enum Consts: String {
        case notificationsEnabled
        case isAuthorized
        case isLocalAuthEnabled
        case pinCodeWasEntered
        case userPhoneNumber
    }
    
    static let defs = UserDefaults.standard
    
    @UserDefaultOptional(key: .notificationsEnabled)
    static var notificationsEnabled: Bool?
    
    @UserDefaultOptional(key: .isAuthorized)
    static var isAuthorized: Bool?
    
    @UserDefault<Bool>(key: .isLocalAuthEnabled, defaultValue: true )
    static var isLocalAuthEnabled: Bool
    
    @UserDefault<Bool>(key: .pinCodeWasEntered, defaultValue: false)
    static var pinCodeWasEntered: Bool
    
    @UserDefaultOptional(key: .userPhoneNumber)
    static var userPhoneNumber: String?
    
    static func reset() {
        let dict = defs.dictionaryRepresentation()
        dict.forEach { (key, _) in
            defs.removeObject(forKey: key)
        }
        defs.synchronize()
    }
}

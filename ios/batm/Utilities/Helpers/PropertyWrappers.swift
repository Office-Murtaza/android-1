import Foundation

/// Don't use this class directly
class UserDefaultBaseWrapper<T> {
    let key: String
    let defaultValue: T
    
    init(key: UserDefaultsHelper.Consts, defaultValue: T) {
        self.key = key.rawValue
        self.defaultValue = defaultValue
    }
}

/// Use this wrapper for the simple types like String, Int, Bool
@propertyWrapper
class UserDefault<T>: UserDefaultBaseWrapper<T> {
    var wrappedValue: T {
        get { UserDefaultsHelper.defs.object(forKey: key) as? T ?? defaultValue }
        set { UserDefaultsHelper.defs.set(newValue, forKey: key) }
    }
}

// MARK: - Optional
// The same, but the wrapped property will be optional

/// Don't use this class directly
class UserDefaultOptionalBaseWrapper<T> {
    let key: String
    
    init(key: UserDefaultsHelper.Consts) {
        self.key = key.rawValue
    }
}

/// Use this wrapper for the simple types like String?, Int?, Bool?
@propertyWrapper
class UserDefaultOptional<T>: UserDefaultOptionalBaseWrapper<T> {
    var wrappedValue: T? {
        get { UserDefaultsHelper.defs.object(forKey: key) as? T }
        set { UserDefaultsHelper.defs.set(newValue, forKey: key) }
    }
}

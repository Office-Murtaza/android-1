//
//  KeychainManager.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 07.12.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation

class KeychainManager {
    static func save(value: String, for key: String) {
        guard let dataFromString = value.data(using: .utf8) else { return }
        
        let keychainQuery: [CFString : Any] = [kSecClass: kSecClassGenericPassword,
                                               kSecAttrService: key,
                                               kSecValueData: dataFromString]
        SecItemDelete(keychainQuery as CFDictionary)
        SecItemAdd(keychainQuery as CFDictionary, nil)
    }
    
    static func removeValue(for key: String) {
        
        let keychainQuery: [CFString : Any] = [kSecClass: kSecClassGenericPassword,
                                               kSecAttrService: key]
        
        SecItemDelete(keychainQuery as CFDictionary)
    }
    
    static func loadValue(for key: String) -> String? {
        let keychainQuery: [CFString : Any] = [kSecClass : kSecClassGenericPassword,
                                               kSecAttrService : key,
                                               kSecReturnData: kCFBooleanTrue,
                                               kSecMatchLimitOne: kSecMatchLimitOne]
        
        var dataTypeRef: AnyObject?
        SecItemCopyMatching(keychainQuery as CFDictionary, &dataTypeRef)
        guard let retrievedData = dataTypeRef as? Data else { return nil }
        
        return String(data: retrievedData, encoding: .utf8)
    }
}

//
//  Optional+Helpers.swift
//  batm
//
//  Created by Dmytro Kolesnyk on 22.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import Foundation

extension Optional where Wrapped == Decimal {
    /// The unwrapped value or zero
    var value: Decimal {
        switch self {
        case .some(let value):
            return value
        default: return 0
        }
    }
    
    func toString() -> String {
        return "\(self ?? 0)"
    }
}

extension Optional where Wrapped == Int {
    func toString() -> String {
        return "\(self ?? 0)"
    }
}

extension Optional where Wrapped == Double {
    func toString() -> String {
        return "\(self ?? 0)"
    }
}

extension Optional where Wrapped == String {
    /// The unwrapped value or empty string
    var value: String {
        switch self {
        case .some(let value):
            return value
        default: return ""
        }
    }
}

extension Optional where Wrapped == Bool {
    /// The unwrapped value or false
    var value: Bool {
        switch self {
        case .some(let value):
            return value
        default: return false
        }
    }
}

//
//  String+Extension.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 08.05.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import Foundation

extension String {
    static func randomString(length: Int) -> String {
        let letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return String((0..<length).map { _ in letters.randomElement()! })
    }
}

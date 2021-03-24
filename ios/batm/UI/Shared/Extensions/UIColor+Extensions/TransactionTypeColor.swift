//
//  TransactionTypeColor.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 16.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

protocol TransactionTypeColorPalette {
    static var background: UIColor { get }
    static var font: UIColor { get }
}

extension UIColor {
    enum TransactionTypeColor {
        enum Gray: TransactionTypeColorPalette {
            static let background = UIColor.lightGray
            static let font = UIColor.darkGray
        }
        enum Green: TransactionTypeColorPalette {
            static let background = UIColor(hexString: "48c583", alpha: 0.15)
            static let font = UIColor(hexString: "48c583")
        }
        enum Yellow: TransactionTypeColorPalette {
            static let background = UIColor(hexString: "edb900", alpha: 0.15)
            static let font = UIColor(hexString: "edb900")
        }
        enum Blue: TransactionTypeColorPalette {
            static let background = UIColor(hexString: "0073e4", alpha: 0.15)
            static let font = UIColor(hexString: "0073e4")
        }
    }
}

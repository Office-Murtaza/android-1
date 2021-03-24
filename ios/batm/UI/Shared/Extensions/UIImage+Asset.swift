//
//  UIImage+Extension.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 17.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

extension UIImage {
    static let unknown = UIImage(named: "active")
    
    enum TransactionStatus {
        static let pending = UIImage(named: "pending")
        static let complete = UIImage(named: "complete")
        static let fail = UIImage(named: "fail")
        static let notExist = UIImage(named: "active")
    }
    
    enum CashStatus {
        static let notAvailable = UIImage(named: "active")
        static let available = UIImage(named: "available")
        static let withdrawn = UIImage(named: "withdrawn")
    }
}

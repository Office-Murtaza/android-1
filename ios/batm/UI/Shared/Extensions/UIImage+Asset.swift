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
    
    enum KYCStatus {
        static let notVerified = UIImage(named: "not_verified")
        static let verificationPending = UIImage(named: "pending")
        static let verificationRejected = UIImage(named: "rejected")
        static let verified = UIImage(named: "verified")
        static let vipVerificationPending = UIImage(named: "pending")
        static let vipVerificationRejected = UIImage(named: "rejected")
        static let vipVerified = UIImage(named: "vip_verified")
    }
}

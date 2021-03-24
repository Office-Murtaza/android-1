//
//  TransactionCashStatus.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 17.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

enum TransactionCashStatus: Int {
    case unknown = 0
    case notAvailable = 1
    case available = 2
    case withdrawn = 3
    
    var verboseValue: String {
        switch self {
        case .notAvailable: return localize(L.CoinDetails.notAvailable)
        case .available: return localize(L.CoinDetails.available)
        case .withdrawn: return localize(L.CoinDetails.withdrawn)
        case .unknown: return localize(L.CoinDetails.unknown)
        }
    }
    
    var associatedImage: UIImage? {
        switch self {
        case .unknown: return UIImage.unknown
        case .notAvailable: return UIImage.CashStatus.notAvailable
        case .available: return UIImage.CashStatus.available
        case .withdrawn: return UIImage.CashStatus.withdrawn
        }
    }
}

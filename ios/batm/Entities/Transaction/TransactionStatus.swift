//
//  TransactionStatus.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 17.03.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import UIKit

enum TransactionStatus: Int {
    case unknown = 0
    case pending = 1
    case complete = 2
    case fail = 3
    case notExist = 4
    
    var verboseValue: String {
        switch self {
        case .unknown: return localize(L.CoinDetails.Status.unknown)
        case .pending: return localize(L.CoinDetails.Status.pending)
        case .complete: return localize(L.CoinDetails.Status.complete)
        case .fail: return localize(L.CoinDetails.Status.fail)
        case .notExist: return localize(L.CoinDetails.Status.notExist)
        }
    }
    
    var associatedImage: UIImage? {
        switch self {
        case .unknown: return UIImage.unknown
        case .pending: return UIImage.TransactionStatus.pending
        case .complete: return UIImage.TransactionStatus.complete
        case .fail: return UIImage.TransactionStatus.fail
        case .notExist: return UIImage.TransactionStatus.notExist
        }
    }
    
    var associatedColor: UIColor {
        switch self {
        case .unknown: return .warmGrey
        case .pending: return .lightGold
        case .complete: return .darkMint
        case .fail: return .tomato
        case .notExist: return .warmGrey
        }
    }
}

//
//  TransactionDetailsStore.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 02.04.2021.
//  Copyright © 2021 Daniel Tischenko. All rights reserved.
//

import Foundation

enum TransactionDetailsAction: Equatable {
    case setupTransactionDetails(transactionDetails: TransactionDetails, coinType: CustomCoinType)
}

struct TransactionDetailsState: Equatable {
    var transactionDetails: TransactionDetails?
    var coinType: CustomCoinType?
}

final class TransactionDetailsStore: ViewStore<TransactionDetailsAction, TransactionDetailsState> {
    override var initialState: TransactionDetailsState {
        return TransactionDetailsState()
    }
    
    override func reduce(state: TransactionDetailsState, action: TransactionDetailsAction) -> TransactionDetailsState {
        var state = state
        
        switch action {
        case .setupTransactionDetails(let transactionDetails, let coinType):
            state.transactionDetails = transactionDetails
            state.coinType = coinType
        }
        
        return state
    }
}

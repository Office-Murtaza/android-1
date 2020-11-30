//
//  DealsStore.swift
//  batm
//
//  Created by Dmytro Kolesnyk2 on 30.11.2020.
//  Copyright © 2020 Daniel Tischenko. All rights reserved.
//

import Foundation

enum DealsAction: Equatable {}

struct DealsState: Equatable {}

final class DealsStore: ViewStore<DealsAction, DealsState> {
  
  override var initialState: DealsState {
    return .init()
  }
  
  override func reduce(state: DealsState, action: DealsAction) -> DealsState {
    var state = state
    
    return state
  }
}
